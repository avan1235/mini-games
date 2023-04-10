package ml.dev.kotlin.minigames.service

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
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
    private val serverLocks = ComputedConcurrentHashMap<ServerName, Mutex> {
        Mutex()
    }
    private val serverStateConnections = ComputedConcurrentHashMap<ServerName, MutableSet<GameStateConnection>> {
        Collections.synchronizedSet(HashSet())
    }
    private val serverDataConnections = ComputedConcurrentHashMap<ServerName, MutableSet<GameDataConnection>> {
        Collections.synchronizedSet(HashSet())
    }
    private val userServerStateConnections = ComputedConcurrentHashMap<UserAtServer, MutableSet<GameStateConnection>> {
        Collections.synchronizedSet(HashSet())
    }
    private val userServerDataConnections = ComputedConcurrentHashMap<UserAtServer, MutableSet<GameDataConnection>> {
        Collections.synchronizedSet(HashSet())
    }
    private val serverGamesStates = ComputedConcurrentHashMap<ServerName, GameState> {
        default()
    }
    private val serverUpdateJobs = HashMap<ServerName, Job>()

    suspend fun addStateConnection(serverName: ServerName, username: Username, connection: GameStateConnection): Unit =
        lockForGame(serverName) {
            val role = if (serverStateConnections[serverName].isEmpty()) {
                safeResetServerBackgroundUpdate(serverName)
                UserRole.Admin
            } else UserRole.Player
            val userAtServer = UserAtServer(serverName, username)
            serverStateConnections[serverName] += connection
            userServerStateConnections[userAtServer] += connection
            val gameState = serverGamesStates[serverName].addUser(username, role)
            updateGameState(serverName, gameState)
        }

    suspend fun addDataConnection(serverName: ServerName, username: Username, connection: GameDataConnection): Unit =
        lockForGame(serverName) {
            val userAtServer = UserAtServer(serverName, username)
            serverDataConnections[serverName] += connection
            userServerDataConnections[userAtServer] += connection
        }

    private fun safeStopServerBackgroundUpdate(serverName: ServerName) {
        serverUpdateJobs.remove(serverName)?.cancel()
    }

    private fun safeResetServerBackgroundUpdate(serverName: ServerName) {
        safeStopServerBackgroundUpdate(serverName)
        updateDelay?.let { serverUpdateJobs[serverName] = updateInBackground(it, serverName) }
    }

    suspend fun removeStateConnection(serverName: ServerName, connection: GameStateConnection): GameState? =
        lockForGame(serverName) {
            val userAtServer = UserAtServer(serverName, connection.username)
            userServerStateConnections[userAtServer] -= connection
            serverStateConnections[serverName] -= connection

            if (serverStateConnections[serverName].isEmpty()) {
                safeStopServerBackgroundUpdate(serverName)
                serverGamesStates[serverName] = default()
                null
            } else if (userServerStateConnections[userAtServer].isEmpty()) {
                val gameState = serverGamesStates[serverName].removeUser(connection.username)
                updateGameState(serverName, gameState)
            } else null
        }

    suspend fun removeDataConnection(serverName: ServerName, connection: GameDataConnection): Unit =
        lockForGame(serverName) {
            val userAtServer = UserAtServer(serverName, connection.username)
            userServerDataConnections[userAtServer] -= connection
            serverDataConnections[serverName] -= connection
        }

    suspend fun updateGameState(
        serverName: ServerName,
        username: Username,
        update: GameUpdate,
    ): GameStateUpdateResult? = lockForGame(serverName) update@{
        val oldGame = serverGamesStates[serverName]
        val userData = oldGame.users[username]
        if (userData?.state != UserState.Approved) return@update UnapprovedGameStateUpdate
        val gameState = update.update(username, oldGame, currMillis = now())
        updateGameState(serverName, gameState)?.let { UpdatedGameState(it) }
    }

    suspend fun updateGameState(
        serverName: ServerName,
        byUser: Username,
        forUser: Username,
        action: UserAction,
    ): GameState? = lockForGame(serverName) update@{
        val gameState = serverGamesStates[serverName].changeUserState(byUser, forUser, action)
        updateGameState(serverName, gameState)
    }

    private suspend fun timeUpdateGameState(
        serverName: ServerName,
    ): GameState? = lockForGame(serverName) update@{
        val gameState = serverGamesStates[serverName].update(currMillis = now())
        updateGameState(serverName, gameState)
    }

    private fun updateGameState(serverName: ServerName, gameState: GameState): GameState? {
        val oldState = serverGamesStates[serverName]
        return if (oldState == gameState) null
        else gameState.also { serverGamesStates[serverName] = it }
    }

    fun state(serverName: ServerName): GameState = serverGamesStates[serverName]

    fun stateConnections(serverName: ServerName): Set<GameStateConnection> =
        serverStateConnections[serverName].toSet()

    fun dataConnections(serverName: ServerName): Set<GameDataConnection> =
        serverDataConnections[serverName].toSet()

    fun stateConnections(serverName: ServerName, username: Username): Set<GameStateConnection> =
        userServerStateConnections[UserAtServer(serverName, username)].toSet()

    fun dataConnections(serverName: ServerName, username: Username): Set<GameDataConnection> =
        userServerDataConnections[UserAtServer(serverName, username)].toSet()

    private suspend inline fun <T> lockForGame(serverName: ServerName, action: () -> T): T =
        serverLocks[serverName].withLock(action = action)

    private fun updateInBackground(delayMillis: Long, serverName: ServerName): Job =
        CoroutineScope(Dispatchers.IO).launch {
            var now = 0L
            while (isActive) {
                tryOrNull update@{
                    val updatedGameState = timeUpdateGameState(serverName) ?: return@update
                    val snapshot = updatedGameState.snapshot()
                    supervisorScope {
                        stateConnections(serverName)
                            .map { sendSnapshot(it, snapshot::get) }
                            .joinAll()
                    }
                    val last = now
                    now = now()
                    val passedMillis = now - last
                    delay(delayMillis - passedMillis)
                }
            }
        }
}

class GameStateConnection(
    val session: WebSocketSession,
    val username: Username,
) {
    override fun hashCode(): Int = session.hashCode()
    override fun equals(other: Any?): Boolean = (other as? GameStateConnection)?.session == session
}

class GameDataConnection(
    val session: WebSocketSession,
    val username: Username,
) {
    override fun hashCode(): Int = session.hashCode()
    override fun equals(other: Any?): Boolean = (other as? GameDataConnection)?.session == session
}

fun CoroutineScope.sendSnapshot(
    connection: GameStateConnection,
    snapshot: (Username) -> GameSnapshot?,
): Job = launch {
    val userSnapshot = snapshot(connection.username) ?: return@launch
    val message = GameStateSnapshotServerMessage(userSnapshot, timestamp = now())
    connection.sendStateSerialized(message)
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun GameStateConnection.sendStateSerialized(content: GameStateSnapshotServerMessage): Unit =
    session.send(Frame.Binary(true, GameSerialization.encodeToByteArray(content)))

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun GameDataConnection.sendDataSerialized(content: GameServerMessage): Unit =
    session.send(Frame.Binary(true, GameSerialization.encodeToByteArray(content)))

@JvmInline
value class ServerName(val name: String)

fun String.toServerName(): ServerName = ServerName(this)

sealed interface GameStateUpdateResult
data class UpdatedGameState(val gameState: GameState) : GameStateUpdateResult
object UnapprovedGameStateUpdate : GameStateUpdateResult

private data class UserAtServer(val serverName: ServerName, val username: Username)

private fun <K, V> ComputedConcurrentHashMap(default: (K) -> V): ComputedMap<K, V> =
    ComputedMap(map = ConcurrentHashMap()) { default(it) }
