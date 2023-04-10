package ml.dev.kotlin.minigames.shared.websocket.client

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.GameServerMessage
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.util.tryOrNull


class GameClient(
    private val gamePath: (String) -> String
) : Closeable, InstanceKeeper.Instance {

    private val wsClient = WebsocketApiClient()
    private val userClient = UserClient()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startPlayingGame(
        accessData: GameAccessData,
        serverMessages: MutableStateFlow<GameServerMessage?>,
        clientMessages: MutableStateFlow<GameClientMessage?>,
        onErrorLogin: () -> Unit = {},
        onErrorReceive: (Exception) -> Unit = {},
        onErrorSend: (Exception) -> Unit = {},
    ) {
        val jwtToken = userClient.loginUser(accessData.userLogin)
            .on(ok = { it }, err = { null }, empty = { null })
        if (jwtToken == null) onErrorLogin()
        else wsClient.webSocket(
            path = gamePath(accessData.serverName),
            jwtToken = jwtToken,
            outputMessages = { processServerMessages(serverMessages, onErrorReceive) },
            inputMessages = { processClientMessages(clientMessages, onErrorSend) }
        )
    }

    override fun close() {
        tryOrNull { wsClient.close() }
        tryOrNull { userClient.onDestroy() }
    }

    override fun onDestroy(): Unit = close()
}

data class GameAccessData(val serverName: String, val userLogin: UserLogin)

@OptIn(ExperimentalSerializationApi::class)
private suspend fun DefaultClientWebSocketSession.processClientMessages(
    clientMessages: MutableStateFlow<GameClientMessage?>,
    onErrorSend: (Exception) -> Unit,
) {
    clientMessages.collect {
        if (it != null) try {
            val message = GameSerialization.encodeToByteArray(it)
            send(message)
        } catch (e: Exception) {
            onErrorSend(e)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun DefaultClientWebSocketSession.processServerMessages(
    serverMessages: MutableStateFlow<GameServerMessage?>,
    onErrorReceive: (Exception) -> Unit,
) {
    for (message in incoming) {
        try {
            message as? Frame.Binary ?: continue
            val game = GameSerialization.decodeFromByteArray<GameServerMessage>(message.readBytes())
            serverMessages.emit(game)
        } catch (e: Exception) {
            onErrorReceive(e)
        }
    }
}

