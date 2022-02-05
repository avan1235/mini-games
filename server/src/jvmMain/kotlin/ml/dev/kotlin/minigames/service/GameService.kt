package ml.dev.kotlin.minigames.service

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.GameJson
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.util.tryOrNull
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class GameService(
  private val updateDelay: Long? = null,
  private val default: () -> GameState,
) {
  private val serverLocks = ComputedMap<ServerName, Mutex>(map = ConcurrentHashMap()) { Mutex() }

  private val serverConnections = ComputedMap<ServerName, MutableSet<GameConnection>> {
    Collections.synchronizedSet(HashSet())
  }

  private val userServerConnections = ComputedMap<UserAtServer, MutableSet<GameConnection>> {
    Collections.synchronizedSet(HashSet())
  }

  private val serverGamesStates = ComputedMap<ServerName, GameState>(map = ConcurrentHashMap()) { default() }

  private val serverUpdateJobs = HashMap<ServerName, Job>()

  suspend fun addConnection(serverName: ServerName, username: Username, connection: GameConnection): Unit =
    lockForGame(serverName) {
      val role = if (serverConnections[serverName].isEmpty()) {
        resetServerBackgroundUpdate(serverName)
        UserRole.Admin
      } else UserRole.Player
      val userAtServer = UserAtServer(serverName, username)
      serverConnections[serverName] += connection
      userServerConnections[userAtServer] += connection
      val gameState = serverGamesStates[serverName].addUser(username, role)
      updateGameState(serverName, gameState)
    }

  private fun stopServerBackgroundUpdate(serverName: ServerName) {
    serverUpdateJobs.remove(serverName)?.cancel()
  }

  private fun resetServerBackgroundUpdate(serverName: ServerName) {
    stopServerBackgroundUpdate(serverName)
    updateDelay?.let { serverUpdateJobs[serverName] = updateInBackground(it, serverName) }
  }

  suspend fun removeConnection(serverName: ServerName, connection: GameConnection): GameState? =
    lockForGame(serverName) {
      val userAtServer = UserAtServer(serverName, connection.username)
      userServerConnections[userAtServer] -= connection
      serverConnections[serverName] -= connection

      if (serverConnections[serverName].isEmpty()) {
        stopServerBackgroundUpdate(serverName)
        serverGamesStates[serverName] = default()
        null
      } else if (userServerConnections[userAtServer].isEmpty()) {
        val gameState = serverGamesStates[serverName].removeUser(connection.username)
        updateGameState(serverName, gameState)
      } else null
    }

  suspend fun updateGameState(
    serverName: ServerName,
    username: Username,
    update: GameUpdate,
  ): GameStateUpdateResult? = lockForGame(serverName) update@{
    val oldGame = serverGamesStates[serverName]
    val userData = oldGame.users[username]
    if (userData?.state != UserState.Approved) return@update UnapprovedGameStateUpdate
    val gameState = update.update(username, oldGame)
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

  fun connections(serverName: ServerName): Set<GameConnection> = serverConnections[serverName]

  fun connections(serverName: ServerName, username: Username): Set<GameConnection> =
    userServerConnections[UserAtServer(serverName, username)]

  private suspend inline fun <T> lockForGame(serverName: ServerName, action: () -> T): T =
    serverLocks[serverName].withLock(action = action)

  private fun updateInBackground(delayMillis: Long, serverName: ServerName): Job =
    CoroutineScope(Dispatchers.IO).launch {
      var now = 0L
      do {
        tryOrNull update@{
          val updateGameState = timeUpdateGameState(serverName) ?: return@update
          val snapshots = ComputedMap<Username, GameSnapshot> { updateGameState.snapshot(it) }
          serverConnections[serverName].map { it.sendSnapshot(snapshots) }.awaitAll()
          val last = now
          now = now()
          val passedMillis = now - last
          delay(delayMillis - passedMillis)
        }
      } while (isActive)
    }
}

class GameConnection(val session: WebSocketSession, val username: Username) {
  override fun hashCode(): Int = session.hashCode()
  override fun equals(other: Any?): Boolean = (other as? GameConnection)?.session == session

  suspend fun sendSnapshot(
    snapshots: ComputedMap<Username, GameSnapshot>,
  ): Deferred<Unit> = coroutineScope {
    async {
      val snapshot = snapshots[username]
      val message = GameStateSnapshotServerMessage(snapshot, timestamp = now())
      session.sendJson(message)
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun WebSocketSession.sendJson(content: GameServerMessage): Unit =
  send(Frame.Text(GameJson.encodeToString(content)))

data class ServerName(val name: String)

inline val String.serverName: ServerName get() = ServerName(this)

sealed interface GameStateUpdateResult
data class UpdatedGameState(val gameState: GameState) : GameStateUpdateResult
object UnapprovedGameStateUpdate : GameStateUpdateResult

private data class UserAtServer(val serverName: ServerName, val username: Username)
