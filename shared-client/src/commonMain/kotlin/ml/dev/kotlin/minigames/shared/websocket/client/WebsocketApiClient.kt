package ml.dev.kotlin.minigames.shared.websocket.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import ml.dev.kotlin.minigames.shared.model.JwtToken
import ml.dev.kotlin.minigames.shared.websocket.WebsocketApiConfig

class WebsocketApiClient : Closeable {

    @OptIn(ExperimentalSerializationApi::class)
    private val wsClient = HttpClient(CLIENT_ENGINE_FACTORY) {
        install(ContentNegotiation) { cbor() }
        followRedirects = true
        expectSuccess = true
        install(WebSockets)
    }

    @ExperimentalCoroutinesApi
    suspend fun webSocket(
            path: String,
            jwtToken: JwtToken,
            outputMessages: suspend DefaultClientWebSocketSession.() -> Unit,
            inputMessages: suspend DefaultClientWebSocketSession.() -> Unit,
    ) {
        wsClient.webSocket(
                request = {
                    method = HttpMethod.Get
                    header(HttpHeaders.Authorization, "Bearer ${jwtToken.value}")
                    url(WebsocketApiConfig.scheme, WebsocketApiConfig.host, DEFAULT_PORT, path)
                },
                block = {
                    val messageOutputRoutine = launch { outputMessages() }
                    val messageInputRoutine = launch { inputMessages() }

                    messageInputRoutine.join()
                    messageOutputRoutine.join()
                }
        )
    }

    override fun close(): Unit = wsClient.close()
}

internal expect val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*>
