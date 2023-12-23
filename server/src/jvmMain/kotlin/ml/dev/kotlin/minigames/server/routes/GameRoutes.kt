package ml.dev.kotlin.minigames.server.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import ml.dev.kotlin.minigames.server.Jwt
import ml.dev.kotlin.minigames.service.GameConnection
import ml.dev.kotlin.minigames.service.GameServerName
import ml.dev.kotlin.minigames.service.GameService
import ml.dev.kotlin.minigames.shared.api.BIRD_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.api.GamePath
import ml.dev.kotlin.minigames.shared.api.SET_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.api.SNAKE_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.util.StringValuesKey
import ml.dev.kotlin.minigames.util.authJwtWebSocket
import ml.dev.kotlin.minigames.util.eprintln
import ml.dev.kotlin.minigames.util.get

private val SET_GAME_HANDLER = GameService { SetGameState.random() }.let(::GameHandler)
private val SNAKE_GAME_HANDLER = GameService(updateDelay = 30) { SnakeGameState.empty() }.let(::GameHandler)
private val BIRD_GAME_HANDLER = GameService(updateDelay = 30) { BirdGameState.empty() }.let(::GameHandler)

fun Application.gameSockets() = routing {
    authJwtGameHandlerWebSockets(SET_GAME_WEBSOCKET, SET_GAME_HANDLER)
    authJwtGameHandlerWebSockets(SNAKE_GAME_WEBSOCKET, SNAKE_GAME_HANDLER)
    authJwtGameHandlerWebSockets(BIRD_GAME_WEBSOCKET, BIRD_GAME_HANDLER)
}

@KtorDsl
private fun Route.authJwtGameHandlerWebSockets(
    gamePathSource: (String) -> GamePath,
    handler: GameHandler,
) {
    val gamePath = gamePathSource("{$SERVER_NAME}")
    authJwtWebSocket(gamePath.statePath, handler::handleState)
    authJwtWebSocket(gamePath.dataPath, handler::handleData)
}

private class GameHandler(
    private val service: GameService,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleState(
        session: DefaultWebSocketServerSession,
        user: Jwt.User,
    ): Unit = with(session) {
        val connection = GameConnection.State(session, user.username)
        val serverName = call.parameters[SERVER_NAME]?.let<String, GameServerName>(::GameServerName) ?: return
        service.addStateConnection(serverName, connection)

        try {
            val initialState = service.state(serverName)
            service.sendAllUpdatedGameState(serverName, initialState)

            for (frame in incoming) {
                frame as? Frame.Binary ?: continue
                val bytes = frame.readBytes()
                val clientMessage = GameSerialization.decodeFromByteArray<GameStateUpdateClientMessage>(bytes)
                service.updateGameStateWithClientMessage(serverName, user.username, clientMessage)
            }
        } catch (e: Exception) {
            eprintln(e.localizedMessage)
        } finally {
            service
                .removeStateConnection(serverName, connection)
                ?.let { finalState -> service.sendAllUpdatedGameState(serverName, finalState) }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun handleData(
        session: DefaultWebSocketServerSession,
        user: Jwt.User,
    ): Unit = with(session) {
        val connection = GameConnection.Data(session, user.username)
        val serverName = call.parameters[SERVER_NAME]?.let(::GameServerName) ?: return
        service.addDataConnection(serverName, connection)

        try {
            for (frame in incoming) {
                frame as? Frame.Binary ?: continue
                val bytes = frame.readBytes()
                val clientMessage = GameSerialization.decodeFromByteArray<GameDataClientMessage>(bytes)
                service.updateGameDataWithClientMessage(serverName, connection.username, clientMessage)
            }
        } catch (e: Exception) {
            eprintln(e.localizedMessage)
        } finally {
            service.removeDataConnection(serverName, connection)
        }
    }
}

private val SERVER_NAME = StringValuesKey("serverName")
