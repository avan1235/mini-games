package ml.dev.kotlin.minigames.server.routes

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import ml.dev.kotlin.minigames.server.Jwt
import ml.dev.kotlin.minigames.service.*
import ml.dev.kotlin.minigames.shared.api.SET_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.api.SNAKE_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.GameJson
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.util.TreeV2Set
import ml.dev.kotlin.minigames.util.eprintln

private val SET_GAME_HANDLER = GameService { SetGameState.random() }
  .let(::GameHandler)

private val SNAKE_GAME_HANDLER =
  GameService(updateDelay = 5) { SnakeGameState.empty(items = TreeV2Set()) }
    .let(::GameHandler)

fun Application.gameSockets() = routing {
  authJwtWebSocket(SET_GAME_WEBSOCKET("{$GAME_NAME}"), SET_GAME_HANDLER::handleGame)
  authJwtWebSocket(SNAKE_GAME_WEBSOCKET("{$GAME_NAME}"), SNAKE_GAME_HANDLER::handleGame)
}

private class GameHandler(
  private val service: GameService
) {
  suspend fun handleGame(session: DefaultWebSocketServerSession, user: Jwt.User): Unit =
    with(session) {
      GameConnection(session, user.username).run {
        val gameName = call.parameters[GAME_NAME]?.gameName ?: return
        val username = user.username
        service.addConnection(gameName, username, this)

        try {
          sendAllUpdatedGameState(gameName, service[gameName])

          for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val clientMessage = GameJson.decodeFromString<GameClientMessage>(frame.readText())
            updateGameStateWithClientMessage(gameName, username, clientMessage)
          }
        } catch (e: Exception) {
          eprintln(e.localizedMessage)
        } finally {
          service.removeConnection(gameName, this)
        }
      }
    }

  private suspend fun GameConnection.updateGameStateWithClientMessage(
    gameName: GameName, username: Username, msg: GameClientMessage
  ): Unit = when (msg) {
    is HeartBeatClientMessage -> service[gameName].let(gameStateServerMessage(username))
      .let { session.sendJson(it) }
    is GameStateUpdateClientMessage -> service.updateGameState(gameName, username, msg.update)
      ?.let { sendGameStateUpdate(gameName, it) }
    is UserActionClientMessage -> service.updateGameState(
      gameName,
      byUser = username,
      forUser = msg.username,
      action = msg.action
    )?.let { sendAllUpdatedGameState(gameName, it) }
  } ?: Unit

  private suspend fun sendAllUpdatedGameState(
    gameName: GameName, gameState: GameState
  ): Unit = service.connections(gameName).forEach {
    val message = gameState.let(gameStateServerMessage(it.username))
    it.session.sendJson(message)
  }

  private suspend fun GameConnection.sendGameStateUpdate(
    gameName: GameName,
    updateResult: GameStateUpdateResult,
  ): Unit = when (updateResult) {
    UnapprovedGameStateUpdate -> UnapprovedGameStateUpdateServerMessage(timestamp = now()).let {
      session.sendJson(
        it
      )
    }
    is UpdatedGameState -> sendAllUpdatedGameState(gameName, updateResult.gameState)
  }
}

private const val GAME_NAME = "gameName"

private fun gameStateServerMessage(forUser: Username) =
  { state: GameState -> GameStateSnapshotServerMessage(state.snapshot(forUser), timestamp = now()) }
