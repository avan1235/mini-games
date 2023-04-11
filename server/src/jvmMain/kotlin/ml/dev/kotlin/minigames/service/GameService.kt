package ml.dev.kotlin.minigames.service

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import ml.dev.kotlin.minigames.service.GameServerLocks.Companion.GameServerGuard
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.util.tryOrNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GameService(
    private val updateDelay: Long? = null,
    private val default: () -> GameState,
) {
    private val locks = GameServerLocks()

    private suspend inline fun <T> lockForGame(serverName: GameServerName, action: GameServerGuard.(GameServerGuard) -> T): T =
        locks.lockForGame(serverName, action)

    private val serverStateConnections = ComputedMap<GameServerGuard, MutableSet<GameConnection.State>> { HashSet() }

    private val serverDataConnections = ComputedMap<GameServerGuard, MutableSet<GameConnection.Data>> { HashSet() }

    private val userServerStateConnections =
        ComputedMap<GameServerGuard, ComputedMap<Username, MutableSet<GameConnection.State>>> { ComputedMap { HashSet() } }

    private val userServerDataConnections =
        ComputedMap<GameServerGuard, ComputedMap<Username, MutableSet<GameConnection.Data>>> { ComputedMap { HashSet() } }

    private val serverGamesStates = ComputedMap<GameServerGuard, GameState> { default() }

    private val serverUpdateJobs = HashMap<GameServerGuard, Job>()

    suspend fun addStateConnection(serverName: GameServerName, username: Username, connection: GameConnection.State): Unit =
        lockForGame(serverName) { guard ->
            val role = if (serverStateConnections[guard].isEmpty()) {
                resetServerBackgroundUpdate()
                UserRole.Admin
            } else UserRole.Player
            serverStateConnections[guard] += connection
            userServerStateConnections[guard][username] += connection
            val gameState = serverGamesStates[guard].addUser(username, role)
            updateGameState(gameState)
        }

    suspend fun addDataConnection(serverName: GameServerName, username: Username, connection: GameConnection.Data): Unit =
        lockForGame(serverName) { guard ->
            serverDataConnections[guard] += connection
            userServerDataConnections[guard][username] += connection
        }

    private fun GameServerGuard.stopServerBackgroundUpdate() {
        serverUpdateJobs.remove(this)?.cancel()
    }

    private fun GameServerGuard.resetServerBackgroundUpdate() {
        stopServerBackgroundUpdate()
        updateDelay?.let { serverUpdateJobs[this] = updateInBackground(it, name) }
    }

    suspend fun removeStateConnection(serverName: GameServerName, connection: GameConnection.State): GameState? =
        lockForGame(serverName) { guard ->
            userServerStateConnections[guard][connection.username] -= connection
            serverStateConnections[guard] -= connection

            if (serverStateConnections[guard].isEmpty()) {
                stopServerBackgroundUpdate()
                serverGamesStates[guard] = default()
                null
            } else if (userServerStateConnections[guard].isEmpty()) {
                val gameState = serverGamesStates[guard].removeUser(connection.username)
                updateGameState(gameState)
            } else null
        }

    suspend fun removeDataConnection(serverName: GameServerName, connection: GameConnection.Data): Unit =
        lockForGame(serverName) { guard ->
            userServerDataConnections[guard][connection.username] -= connection
            serverDataConnections[guard] -= connection
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
            forUser = msg.username,
            action = msg.action
        )?.let { gameState ->
            val message = UserActionServerMessage(action = msg.action, timestamp = now())
            val connections = dataConnections(serverName, msg.username)
            connections.forEach { it.sendSerialized(message) }
            sendAllUpdatedGameState(serverName, gameState)
        }

        is SendMessageClientMessage -> sendAllUserMessage(serverName, msg.message)
    } ?: Unit

    suspend fun updateGameStateWithClientMessage(
        serverName: GameServerName,
        username: Username,
        msg: GameStateUpdateClientMessage,
    ): Unit = when (val updateResult = updateGameState(serverName, username, msg.update)) {
        UnapprovedGameStateUpdate -> sendUnapprovedGameStateUpdate(serverName, username)
        is UpdatedGameState -> sendAllUpdatedGameState(serverName, updateResult.gameState)
        null -> Unit
    }

    private suspend fun updateGameState(
        serverName: GameServerName,
        username: Username,
        update: GameUpdate,
    ): GameStateUpdateResult? = lockForGame(serverName) update@{ guard ->
        val oldGame = serverGamesStates[guard]
        val userData = oldGame.users[username]
        if (userData?.state != UserState.Approved) return@update UnapprovedGameStateUpdate
        val gameState = update.update(username, oldGame, currMillis = now())
        updateGameState(gameState)?.let { UpdatedGameState(it) }
    }

    private suspend fun updateGameState(
        serverName: GameServerName,
        byUser: Username,
        forUser: Username,
        action: UserAction,
    ): GameState? = lockForGame(serverName) { guard ->
        val gameState = serverGamesStates[guard].changeUserState(byUser, forUser, action)
        updateGameState(gameState)
    }

    private suspend fun timeUpdateGameState(
        serverName: GameServerName,
    ): GameState? = lockForGame(serverName) { guard ->
        val gameState = serverGamesStates[guard].update(currMillis = now())
        updateGameState(gameState)
    }

    private fun GameServerGuard.updateGameState(gameState: GameState): GameState? {
        val oldState = serverGamesStates[this]
        return if (oldState == gameState) null
        else gameState.also { serverGamesStates[this] = it }
    }

    suspend fun state(serverName: GameServerName): GameState =
        lockForGame(serverName) { guard -> serverGamesStates[guard] }

    private suspend inline fun stateConnections(serverName: GameServerName): Set<GameConnection.State> =
        lockForGame(serverName) { guard -> serverStateConnections[guard].toSet() }

    private suspend inline fun dataConnections(serverName: GameServerName): Set<GameConnection.Data> =
        lockForGame(serverName) { guard -> serverDataConnections[guard].toSet() }

    private suspend inline fun dataConnections(serverName: GameServerName, username: Username): Set<GameConnection.Data> =
        lockForGame(serverName) { guard -> userServerDataConnections[guard][username].toSet() }

    suspend fun sendAllUpdatedGameState(
        serverName: GameServerName,
        gameState: GameState,
    ) {
        val snapshot = gameState.snapshot()
        supervisorScope {
            stateConnections(serverName)
                .map { launchSendSnapshot(it, snapshot::get) }
                .joinAll()
        }
    }

    private suspend fun sendAllUserMessage(
        serverName: GameServerName,
        userMessage: UserMessage,
    ): Unit = supervisorScope {
        dataConnections(serverName).map {
            launch { it.sendSerialized(ReceiveMessageServerMessage(userMessage, timestamp = now())) }
        }.joinAll()
    }

    private suspend fun sendUnapprovedGameStateUpdate(serverName: GameServerName, username: Username) {
        val message = UnapprovedGameStateUpdateServerMessage(timestamp = now())
        dataConnections(serverName, username).forEach { it.sendSerialized(message) }
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
    private val serverLocks: MutableMap<GameServerName, Mutex> =
        Collections.synchronizedMap(ComputedMap { Mutex() })

    suspend inline fun <T> lockForGame(serverName: GameServerName, action: GameServerGuard.(GameServerGuard) -> T): T =
        serverLocks[serverName]!!.withLock {
            val guard = GameServerGuard(serverName)
            guard.action(guard)
        }

    companion object {
        @JvmInline
        value class GameServerGuard(val name: GameServerName)
    }
}

@JvmInline
value class GameServerName(val name: String)

fun String.toGameServerName(): GameServerName = GameServerName(this)

sealed interface GameStateUpdateResult
data class UpdatedGameState(val gameState: GameState) : GameStateUpdateResult
object UnapprovedGameStateUpdate : GameStateUpdateResult

private fun <K, V> ComputedConcurrentHashMap(default: (K) -> V): ComputedMap<K, V> =
    ComputedMap(map = ConcurrentHashMap()) { default(it) }
