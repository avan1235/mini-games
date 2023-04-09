package ml.dev.kotlin.minigames.server.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import ml.dev.kotlin.minigames.server.Jwt
import ml.dev.kotlin.minigames.service.*
import ml.dev.kotlin.minigames.shared.api.BIRD_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.api.SET_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.api.SNAKE_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.util.StringValuesKey
import ml.dev.kotlin.minigames.util.authJwtWebSocket
import ml.dev.kotlin.minigames.util.eprintln
import ml.dev.kotlin.minigames.util.get

private val SET_GAME_HANDLER = GameService { SetGameState.random() }.let(::GameHandler)

private val SNAKE_GAME_HANDLER = GameService(updateDelay = 20) { SnakeGameState.empty() }.let(::GameHandler)

private val BIRD_GAME_HANDLER = GameService(updateDelay = 20) { BirdGameState.empty() }.let(::GameHandler)

fun Application.gameSockets() = routing {
    authJwtWebSocket(SET_GAME_WEBSOCKET("{$SERVER_NAME}"), SET_GAME_HANDLER::handleGame)
    authJwtWebSocket(SNAKE_GAME_WEBSOCKET("{$SERVER_NAME}"), SNAKE_GAME_HANDLER::handleGame)
    authJwtWebSocket(BIRD_GAME_WEBSOCKET("{$SERVER_NAME}"), BIRD_GAME_HANDLER::handleGame)
}

private class GameHandler(
        private val service: GameService,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleGame(
            session: DefaultWebSocketServerSession,
            user: Jwt.User,
    ) {
        with(session) {
            GameConnection(session, user.username).run {
                val serverName = call.parameters[SERVER_NAME]?.toServerName() ?: return
                val username = user.username
                service.addConnection(serverName, username, this)

                try {
                    sendAllUpdatedGameState(serverName, service.state(serverName))

                    for (frame in incoming) {
                        frame as? Frame.Binary ?: continue
                        val clientMessage = GameSerialization.decodeFromByteArray<GameClientMessage>(frame.readBytes())
                        updateGameStateWithClientMessage(serverName, username, clientMessage)
                    }
                } catch (e: Exception) {
                    eprintln(e.localizedMessage)
                } finally {
                    service
                            .removeConnection(serverName, this)
                            ?.let { gameState -> sendAllUpdatedGameState(serverName, gameState) }
                }
            }
        }
    }

    private suspend fun GameConnection.updateGameStateWithClientMessage(
            serverName: ServerName,
            username: Username,
            msg: GameClientMessage,
    ): Unit = when (msg) {
        is HeartBeatClientMessage -> service.state(serverName)
                .let(asGameStateServerMessage(username))
                .let { session.sendSerialized(it) }

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
            connections.forEach { it.session.sendSerialized(message) }
            sendAllUpdatedGameState(serverName, gameState)
        }

        is SendMessageClientMessage -> sendAllUserMessage(serverName, msg.message)
    } ?: Unit

    private suspend fun sendAllUpdatedGameState(
            serverName: ServerName,
            gameState: GameState,
    ): Unit = service.connections(serverName).forEach {
        val message = gameState.let(asGameStateServerMessage(it.username))
        it.session.sendSerialized(message)
    }

    private suspend fun sendAllUserMessage(
            serverName: ServerName,
            userMessage: UserMessage,
    ): Unit = service.connections(serverName).forEach {
        val message = ReceiveMessageServerMessage(userMessage, timestamp = now())
        it.session.sendSerialized(message)
    }

    private suspend fun GameConnection.sendGameStateUpdate(
            serverName: ServerName,
            updateResult: GameStateUpdateResult,
    ): Unit = when (updateResult) {
        UnapprovedGameStateUpdate -> UnapprovedGameStateUpdateServerMessage(timestamp = now())
                .let { session.sendSerialized(it) }

        is UpdatedGameState -> sendAllUpdatedGameState(serverName, updateResult.gameState)
    }
}

private val SERVER_NAME = StringValuesKey("serverName")

private fun asGameStateServerMessage(forUser: Username) =
        fun(state: GameState) = GameStateSnapshotServerMessage(state.snapshot(forUser), timestamp = now())
