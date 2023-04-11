package ml.dev.kotlin.minigames.shared.websocket.client

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import ml.dev.kotlin.minigames.shared.api.GamePath
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.util.GameSerialization
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.util.tryOrNull


class GameClient constructor(
    private val gamePath: (serverName: String) -> GamePath
) : Closeable, InstanceKeeper.Instance {

    private val wsStateClient = WebsocketApiClient()
    private val wsDataClient = WebsocketApiClient()
    private val userClient = UserClient()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startPlayingGame(
        accessData: GameAccessData,

        serverMessages: MutableSharedFlow<GameDataServerMessage>,
        serverStateMessages: MutableSharedFlow<GameStateSnapshotServerMessage?>,

        clientMessages: SharedFlow<GameDataClientMessage>,
        clientStateMessages: SharedFlow<GameStateUpdateClientMessage?>,

        onErrorLogin: () -> Unit = {},
        onErrorReceive: (Exception) -> Unit = {},
        onErrorSend: (Exception) -> Unit = {},
    ) {
        val jwtToken = userClient.loginUser(accessData.userLogin)
            .on(ok = { it }, err = { null }, empty = { null })
        if (jwtToken == null) onErrorLogin()
        else coroutineScope {
            launch {
                wsDataClient.webSocket(
                    path = gamePath(accessData.serverName).dataPath,
                    jwtToken = jwtToken,
                    outputMessages = { receiveMessages(serverMessages, onErrorReceive) },
                    inputMessages = { sendMessages(clientMessages, onErrorSend) }
                )
            }
            launch {
                wsStateClient.webSocket(
                    path = gamePath(accessData.serverName).statePath,
                    jwtToken = jwtToken,
                    outputMessages = { receiveMessages(serverStateMessages, onErrorReceive) },
                    inputMessages = { sendMessages(clientStateMessages, onErrorSend) }
                )
            }
        }
    }

    override fun close() {
        tryOrNull { wsDataClient.close() }
        tryOrNull { wsStateClient.close() }
        tryOrNull { userClient.onDestroy() }
    }

    override fun onDestroy(): Unit = close()
}

data class GameAccessData(val serverName: String, val userLogin: UserLogin)

@OptIn(ExperimentalSerializationApi::class)
private suspend inline fun <reified T> DefaultClientWebSocketSession.sendMessages(
    source: SharedFlow<T>,
    crossinline onErrorSend: (Exception) -> Unit,
) {
    source
        .filterNot { it == null }
        .collect {
            try {
                val message = GameSerialization.encodeToByteArray(it)
                send(message)
            } catch (e: Exception) {
                Napier.e { "${T::class.simpleName} ${e.stackTraceToString()}" }
                onErrorSend(e)
            }
        }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend inline fun <reified T> DefaultClientWebSocketSession.receiveMessages(
    outlet: MutableSharedFlow<T>,
    crossinline onErrorReceive: (Exception) -> Unit,
) {
    for (message in incoming) {
        try {
            message as? Frame.Binary ?: continue
            val game = GameSerialization.decodeFromByteArray<T>(message.readBytes())
            outlet.emit(game)
        } catch (e: Exception) {
            Napier.e { "${T::class.simpleName} ${e.stackTraceToString()}" }
            onErrorReceive(e)
        }
    }
}
