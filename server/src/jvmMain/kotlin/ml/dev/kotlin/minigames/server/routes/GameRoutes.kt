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
    authJwtWebSocket(SET_GAME_WEBSOCKET("{$SERVER_NAME}").statePath, SET_GAME_HANDLER::handleState)
    authJwtWebSocket(SET_GAME_WEBSOCKET("{$SERVER_NAME}").dataPath, SET_GAME_HANDLER::handleData)

    authJwtWebSocket(SNAKE_GAME_WEBSOCKET("{$SERVER_NAME}").statePath, SNAKE_GAME_HANDLER::handleState)
    authJwtWebSocket(SNAKE_GAME_WEBSOCKET("{$SERVER_NAME}").dataPath, SNAKE_GAME_HANDLER::handleData)

    authJwtWebSocket(BIRD_GAME_WEBSOCKET("{$SERVER_NAME}").statePath, BIRD_GAME_HANDLER::handleState)
    authJwtWebSocket(BIRD_GAME_WEBSOCKET("{$SERVER_NAME}").dataPath, BIRD_GAME_HANDLER::handleData)
}

private class GameHandler(
    private val service: GameService,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleState(
        session: DefaultWebSocketServerSession,
        user: Jwt.User,
    ) {
        with(session) {
            GameStateConnection(session, user.username).run {
                val serverName = call.parameters[SERVER_NAME]?.toServerName() ?: return
                val username = user.username
                service.addStateConnection(serverName, username, this)

                try {
                    sendAllUpdatedGameState(serverName, service.state(serverName))

                    for (frame in incoming) {
                        frame as? Frame.Binary ?: continue
                        val bytes = frame.readBytes()
                        val clientMessage = GameSerialization.decodeFromByteArray<GameStateUpdateClientMessage>(bytes)
                        updateGameStateWithClientMessage(serverName, username, clientMessage)
                    }
                } catch (e: Exception) {
                    eprintln(e.localizedMessage)
                } finally {
                    service
                        .removeStateConnection(serverName, this)
                        ?.let { gameState -> sendAllUpdatedGameState(serverName, gameState) }
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleData(
        session: DefaultWebSocketServerSession,
        user: Jwt.User,
    ) {
        with(session) {
            GameDataConnection(session, user.username).run {
                val serverName = call.parameters[SERVER_NAME]?.toServerName() ?: return
                val username = user.username
                service.addDataConnection(serverName, username, this)

                try {
                    for (frame in incoming) {
                        frame as? Frame.Binary ?: continue
                        val bytes = frame.readBytes()
                        val clientMessage = GameSerialization.decodeFromByteArray<GameClientMessage>(bytes)
                        updateGameDataWithClientMessage(serverName, username, clientMessage)
                    }
                } catch (e: Exception) {
                    eprintln(e.localizedMessage)
                } finally {
                    service.removeDataConnection(serverName, this)
                }
            }
        }
    }

    private suspend fun GameDataConnection.updateGameDataWithClientMessage(
        serverName: ServerName,
        username: Username,
        msg: GameClientMessage,
    ): Unit = when (msg) {
        is HeartBeatClientMessage -> service.state(serverName)
            .let(asGameStateServerMessage(username))
            .let { state ->
                service.stateConnections(serverName, username)
                    .forEach { it.sendStateSerialized(state) }
            }

        is UserActionClientMessage -> service.updateGameState(
            serverName = serverName,
            byUser = username,
            forUser = msg.username,
            action = msg.action
        )?.let { gameState ->
            val message = UserActionServerMessage(action = msg.action, timestamp = now())
            val connections = service.dataConnections(serverName, msg.username)
            connections.forEach { it.sendDataSerialized(message) }
            sendAllUpdatedGameState(serverName, gameState)
        }

        is SendMessageClientMessage -> sendAllUserMessage(serverName, msg.message)
    } ?: Unit

    private suspend fun GameStateConnection.updateGameStateWithClientMessage(
        serverName: ServerName,
        username: Username,
        msg: GameStateUpdateClientMessage,
    ): Unit = service
        .updateGameState(serverName, username, msg.update)
        ?.let { sendGameStateUpdate(serverName, it) }
        ?: Unit

    private suspend fun sendAllUpdatedGameState(
        serverName: ServerName,
        gameState: GameState,
    ): Unit = service.stateConnections(serverName).forEach {
        val message = gameState.let(asGameStateServerMessage(it.username))
        it.sendStateSerialized(message)
    }

    private suspend fun sendAllUserMessage(
        serverName: ServerName,
        userMessage: UserMessage,
    ): Unit = service.dataConnections(serverName).forEach {
        val message = ReceiveMessageServerMessage(userMessage, timestamp = now())
        it.sendDataSerialized(message)
    }

    private suspend fun GameStateConnection.sendGameStateUpdate(
        serverName: ServerName,
        updateResult: GameStateUpdateResult,
    ): Unit = when (updateResult) {
        UnapprovedGameStateUpdate -> UnapprovedGameStateUpdateServerMessage(timestamp = now())
            .let { data ->
                service.dataConnections(serverName)
                    .forEach { it.sendDataSerialized(data) }
            }

        is UpdatedGameState -> sendAllUpdatedGameState(serverName, updateResult.gameState)
    }
}

private val SERVER_NAME = StringValuesKey("serverName")

private fun asGameStateServerMessage(forUser: Username) =
    fun(state: GameState) = GameStateSnapshotServerMessage(state.snapshot(forUser), timestamp = now())
