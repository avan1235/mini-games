package ml.dev.kotlin.minigames.service

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.model.UserActionClientMessage.UserAction
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.GameJson
import ml.dev.kotlin.minigames.shared.util.now
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GameService(
  updateDelay: Long? = null,
  private val default: () -> GameState,
) {

  private val gameLock = ComputedMap<GameName, Mutex>(map = ConcurrentHashMap()) { Mutex() }

  private val connections = ComputedMap<GameName, MutableSet<GameConnection>> {
    Collections.synchronizedSet(LinkedHashSet())
  }

  private val gameStates = ComputedMap<GameName, GameState>(map = ConcurrentHashMap()) { default() }

  init {
    updateDelay?.let { updateInBackground(it) }
  }

  suspend fun addConnection(gameName: GameName, username: Username, connection: GameConnection): Unit =
    lockForGame(gameName) {
      val role = if (connections[gameName].isEmpty()) UserRole.Admin else UserRole.Player
      connections[gameName] += connection
      val gameState = this[gameName].addUser(username, role)
      updateGameState(gameName, gameState)
    }

  suspend fun removeConnection(gameName: GameName, connection: GameConnection): Unit = lockForGame(gameName) {
    connections[gameName] -= connection
  }

  suspend fun updateGameState(
    gameName: GameName,
    username: Username,
    update: GameUpdate,
  ): GameStateUpdateResult? = lockForGame(gameName) update@{
    val oldGame = this[gameName]
    val userData = oldGame.users[username]
    if (userData?.state != UserState.Approved) return@update UnapprovedGameStateUpdate
    val gameState = update.update(username, oldGame)
    updateGameState(gameName, gameState)?.let { UpdatedGameState(it) }
  }

  suspend fun updateGameState(
    gameName: GameName,
    byUser: Username,
    forUser: Username,
    action: UserAction,
  ): GameState? = lockForGame(gameName) update@{
    val gameState = this[gameName].changeUserState(byUser, forUser, action)
    updateGameState(gameName, gameState)
  }

  private suspend fun updateGameState(
    gameName: GameName,
  ): GameState? = lockForGame(gameName) update@{
    val gameState = this[gameName].update(currMillis = now())
    updateGameState(gameName, gameState)
  }

  private fun updateGameState(gameName: GameName, gameState: GameState): GameState? {
    val oldState = gameStates[gameName]
    return if (oldState == gameState) null
    else gameState.also { gameStates[gameName] = it }
  }

  fun connections(gameName: GameName): Set<GameConnection> = connections[gameName]

  private suspend inline fun <T> lockForGame(gameName: GameName, action: () -> T): T =
    gameLock[gameName].withLock(action = action)

  operator fun get(gameName: GameName): GameState = gameStates[gameName]

  private fun updateInBackground(delayMillis: Long): Job = CoroutineScope(Dispatchers.IO).launch {
    var now = 0L
    do {
      gameStates.keys.toList().forEach {
        val updateGameState = updateGameState(it) ?: return@forEach
        connections[it].forEach { connection ->
          val snapshot = updateGameState.snapshot(connection.username)
          val message = GameStateSnapshotServerMessage(snapshot, timestamp = now())
          connection.session.sendJson(message)
        }
      }
      val last = now
      now = now()
      val passedMillis = now - last
      delay(delayMillis - passedMillis)
    } while (isActive)
  }
}

class GameConnection(val session: WebSocketSession, val username: Username) {
  override fun hashCode(): Int = session.hashCode()
  override fun equals(other: Any?): Boolean = (other as? GameConnection)?.session == session
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun WebSocketSession.sendJson(content: GameServerMessage): Unit =
  send(Frame.Text(GameJson.encodeToString(content)))

data class GameName(val name: String)

inline val String.gameName: GameName get() = GameName(this)

sealed interface GameStateUpdateResult
data class UpdatedGameState(val gameState: GameState) : GameStateUpdateResult
object UnapprovedGameStateUpdate : GameStateUpdateResult
