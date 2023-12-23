package ml.dev.kotlin.minigames.service

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import ml.dev.kotlin.minigames.service.GameServerLocks.Companion.GameServerGuard
import ml.dev.kotlin.minigames.service.GameStateUpdateResult.Unapproved
import ml.dev.kotlin.minigames.service.GameStateUpdateResult.Updated
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.util.tryOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@JvmInline
value class GameServerName(val name: String)

class GameService(
    private val updateDelay: Long? = null,
    private val default: () -> GameState,
) {
    private val locks = GameServerLocks()

    private suspend inline fun <T> lockForGame(serverName: GameServerName, action: GameServerGuard.() -> T): T =
        locks.lockForGame(serverName, action)

    private val serverStateConnections = ConcurrentHashMap<GameServerName, CopyOnWriteArrayList<GameConnection.State>>()

    private val serverDataConnections = ConcurrentHashMap<GameServerName, CopyOnWriteArrayList<GameConnection.Data>>()

    private val serverUpdateJobs = ConcurrentHashMap<GameServerName, Job>()

    private val serverGamesStates = ComputedMap<GameServerGuard, GameState> { default() }

    suspend fun addStateConnection(serverName: GameServerName, connection: GameConnection.State) {
        val connections = serverStateConnections.safeGet(serverName)
        val role = if (connections.isEmpty()) {
            resetServerBackgroundUpdate(serverName)
            UserRole.Admin
        } else UserRole.Player
        connections += connection
        lockForGame(serverName) {
            val gameState = serverGamesStates[this].addUser(connection.username, role)
            updateGameState(gameState)
        }
    }

    fun addDataConnection(serverName: GameServerName, connection: GameConnection.Data) {
        serverDataConnections.safeGet(serverName) += connection
    }

    private fun stopServerBackgroundUpdate(serverName: GameServerName) {
        serverUpdateJobs.remove(serverName)?.cancel()
    }

    private fun resetServerBackgroundUpdate(serverName: GameServerName) {
        if (updateDelay == null) return
        val job = updateInBackground(updateDelay, serverName)
        serverUpdateJobs.put(serverName, job)?.cancel()
    }

    suspend fun removeStateConnection(
        serverName: GameServerName,
        connection: GameConnection.State,
    ): GameState? {
        val connections = serverStateConnections.safeGet(serverName)
        connections -= connection

        return if (connections.isEmpty()) {
            stopServerBackgroundUpdate(serverName)
            lockForGame(serverName) { serverGamesStates[this] = default() }
            null
        } else if (connections.none { it.username == connection.username }) lockForGame(serverName) {
            val gameState = serverGamesStates[this].removeUser(connection.username)
            updateGameState(gameState)
        } else null
    }

    fun removeDataConnection(serverName: GameServerName, connection: GameConnection.Data) {
        serverDataConnections.safeGet(serverName) -= connection
    }

    suspend fun updateGameDataWithClientMessage(
        serverName: GameServerName,
        username: Username,
        msg: GameDataClientMessage,
    ): Unit = when (msg) {
        is HeartBeatClientMessage -> sendAllUpdatedGameState(serverName, state(serverName))

        is UserActionClientMessage -> updateGameState(
            serverName = serverName,
            byUser = username,
            forUser = msg.forUsername,
            action = msg.action
        )?.let { gameState ->
            val message = UserActionServerMessage(action = msg.action, timestamp = now())
            val connections = serverDataConnections.safeGet(serverName)
            connections.forEach { if (it.username == msg.forUsername) it.sendSerialized(message) }
            sendAllUpdatedGameState(serverName, gameState)
        }

        is SendMessageClientMessage -> sendAllUserMessage(serverName, msg.message)
    } ?: Unit

    suspend fun updateGameStateWithClientMessage(
        serverName: GameServerName,
        username: Username,
        msg: GameStateUpdateClientMessage,
    ): Unit = when (val updateResult = updateGameState(serverName, username, msg.update)) {
        Unapproved -> sendUnapprovedGameStateUpdate(serverName, username)
        is Updated -> sendAllUpdatedGameState(serverName, updateResult.gameState)
        null -> Unit
    }

    private suspend fun updateGameState(
        serverName: GameServerName,
        username: Username,
        update: GameUpdate,
    ): GameStateUpdateResult? = lockForGame(serverName) update@{
        val oldGame = serverGamesStates[this]
        val userData = oldGame.users[username]
        if (userData?.state != UserState.Approved) return@update Unapproved
        val gameState = update.update(username, oldGame, currMillis = now())
        updateGameState(gameState)?.let { Updated(it) }
    }

    private suspend fun updateGameState(
        serverName: GameServerName,
        byUser: Username,
        forUser: Username,
        action: UserAction,
    ): GameState? = lockForGame(serverName) {
        val gameState = serverGamesStates[this].changeUserState(byUser, forUser, action)
        updateGameState(gameState)
    }

    private suspend fun timeUpdateGameState(
        serverName: GameServerName,
    ): GameState? = lockForGame(serverName) {
        val gameState = serverGamesStates[this].update(currMillis = now())
        updateGameState(gameState)
    }

    private fun GameServerGuard.updateGameState(gameState: GameState): GameState? {
        val oldState = serverGamesStates[this]
        return if (oldState == gameState) null
        else gameState.also { serverGamesStates[this] = it }
    }

    suspend fun state(serverName: GameServerName): GameState =
        lockForGame(serverName) { serverGamesStates[this] }

    suspend fun sendAllUpdatedGameState(
        serverName: GameServerName,
        gameState: GameState,
    ) {
        val snapshot = gameState.snapshot()
        supervisorScope {
            serverStateConnections.safeGet(serverName)
                .map { launchSendSnapshot(it, snapshot::get) }
                .joinAll()
        }
    }

    private suspend fun sendAllUserMessage(
        serverName: GameServerName,
        userMessage: UserMessage,
    ): Unit = supervisorScope {
        serverDataConnections.safeGet(serverName).map {
            launch { it.sendSerialized(ReceiveMessageServerMessage(userMessage, timestamp = now())) }
        }.joinAll()
    }

    private suspend fun sendUnapprovedGameStateUpdate(serverName: GameServerName, username: Username) {
        val message = UnapprovedGameStateUpdateServerMessage(timestamp = now())
        serverDataConnections.safeGet(serverName).forEach {
            if (it.username == username) it.sendSerialized(message)
        }
    }

    private fun updateInBackground(delayMillis: Long, serverName: GameServerName): Job =
        CoroutineScope(Dispatchers.IO).launch {
            var now = 0L
            while (isActive) tryOrNull update@{
                val updatedGameState = timeUpdateGameState(serverName) ?: return@update
                sendAllUpdatedGameState(serverName, updatedGameState)

                val last = now
                now = now()
                val passedMillis = now - last
                delay(delayMillis - passedMillis)
            }
        }
}

sealed class GameConnection(
    protected val session: WebSocketSession,
    val username: Username,
) {
    override fun hashCode(): Int = session.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this::class.java != other::class.java) return false
        return (other as? GameConnection)?.session == session
    }

    class State(session: WebSocketSession, username: Username) : GameConnection(session, username) {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun sendSerialized(content: GameStateSnapshotServerMessage): Unit =
            session.send(Frame.Binary(true, GameSerialization.encodeToByteArray(content)))
    }

    class Data(session: WebSocketSession, username: Username) : GameConnection(session, username) {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun sendSerialized(content: GameDataServerMessage): Unit =
            session.send(Frame.Binary(true, GameSerialization.encodeToByteArray(content)))
    }
}

private fun CoroutineScope.launchSendSnapshot(
    connection: GameConnection.State,
    snapshot: (Username) -> GameSnapshot?,
): Job = launch {
    val userSnapshot = snapshot(connection.username) ?: return@launch
    val message = GameStateSnapshotServerMessage(userSnapshot, timestamp = now())
    connection.sendSerialized(message)
}

private class GameServerLocks {
    private val serverLocks: ConcurrentHashMap<GameServerName, Mutex> = ConcurrentHashMap()

    suspend inline fun <T> lockForGame(serverName: GameServerName, action: GameServerGuard.() -> T): T =
        serverLocks.getOrPut(serverName) { Mutex() }
            .withLock {
                val guard = GameServerGuard(serverName)
                guard.action()
            }

    companion object {
        @JvmInline
        value class GameServerGuard(val name: GameServerName)
    }
}

private sealed interface GameStateUpdateResult {
    data object Unapproved : GameStateUpdateResult

    @JvmInline
    value class Updated(val gameState: GameState) : GameStateUpdateResult
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <K, V> ConcurrentHashMap<K, CopyOnWriteArrayList<V>>.safeGet(key: K): CopyOnWriteArrayList<V> =
    getOrPut(key) { CopyOnWriteArrayList() }