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
import ml.dev.kotlin.minigames.util.eprintln

private val SET_GAME_HANDLER = GameService { SetGameState.random() }
  .let(::GameHandler)

private val SNAKE_GAME_HANDLER = GameService(updateDelay = 5) { SnakeGameState.empty() }.let(::GameHandler)

fun Application.gameSockets() = routing {
  authJwtWebSocket(SET_GAME_WEBSOCKET("{$SERVER_NAME}"), SET_GAME_HANDLER::handleGame)
  authJwtWebSocket(SNAKE_GAME_WEBSOCKET("{$SERVER_NAME}"), SNAKE_GAME_HANDLER::handleGame)
}

private class GameHandler(
  private val service: GameService
) {
  suspend fun handleGame(session: DefaultWebSocketServerSession, user: Jwt.User): Unit =
    with(session) {
      GameConnection(session, user.username).run {
        val serverName = call.parameters[SERVER_NAME]?.serverName ?: return
        val username = user.username
        service.addConnection(serverName, username, this)

        try {
          sendAllUpdatedGameState(serverName, service.state(serverName))

          for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val clientMessage = GameJson.decodeFromString<GameClientMessage>(frame.readText())
            updateGameStateWithClientMessage(serverName, username, clientMessage)
          }
        } catch (e: Exception) {
          eprintln(e.localizedMessage)
        } finally {
          service.removeConnection(serverName, this)
        }
      }
    }

  private suspend fun GameConnection.updateGameStateWithClientMessage(
    serverName: ServerName, username: Username, msg: GameClientMessage
  ): Unit = when (msg) {
    is HeartBeatClientMessage -> service.state(serverName)
      .let(gameStateServerMessage(username))
      .let { session.sendJson(it) }
    is GameStateUpdateClientMessage -> service
      .updateGameState(serverName, username, msg.update)
      ?.let { sendGameStateUpdate(serverName, it) }
    is UserActionClientMessage -> service.updateGameState(
      serverName = serverName,
      byUser = username,
      forUser = msg.username,
      action = msg.action
    )?.let { gameState ->
      val message = UserActionServerMessage(action = msg.action, timestamp = now())
      val connections = service.connections(serverName, msg.username)
      connections.forEach { it.session.sendJson(message) }
      sendAllUpdatedGameState(serverName, gameState)
    }
    is SendMessageClientMessage -> sendAllUserMessage(serverName, msg.message)
  } ?: Unit

  private suspend fun sendAllUpdatedGameState(
    serverName: ServerName,
    gameState: GameState
  ): Unit = service.connections(serverName).forEach {
    val message = gameState.let(gameStateServerMessage(it.username))
    it.session.sendJson(message)
  }

  private suspend fun sendAllUserMessage(
    serverName: ServerName,
    userMessage: UserMessage
  ): Unit = service.connections(serverName).forEach {
    val message = ReceiveMessageServerMessage(userMessage, timestamp = now())
    it.session.sendJson(message)
  }

  private suspend fun GameConnection.sendGameStateUpdate(
    serverName: ServerName,
    updateResult: GameStateUpdateResult,
  ): Unit = when (updateResult) {
    UnapprovedGameStateUpdate -> UnapprovedGameStateUpdateServerMessage(timestamp = now())
      .let { session.sendJson(it) }
    is UpdatedGameState -> sendAllUpdatedGameState(serverName, updateResult.gameState)
  }
}

private const val SERVER_NAME = "serverName"

private fun gameStateServerMessage(forUser: Username) =
  { state: GameState -> GameStateSnapshotServerMessage(state.snapshot(forUser), timestamp = now()) }
