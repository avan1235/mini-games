package ml.dev.kotlin.minigames.shared.websocket.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
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

    suspend fun webSocket(
        path: String,
        jwtToken: JwtToken,
        outputMessages: suspend DefaultClientWebSocketSession.() -> Unit,
        inputMessages: suspend DefaultClientWebSocketSession.() -> Unit,
        onError: (Exception) -> Unit,
    ) {
        wsClient.webSocket(
            request = {
                method = HttpMethod.Get
                header(HttpHeaders.Authorization, "Bearer ${jwtToken.value}")
                parameter(HttpHeaders.Authorization, jwtToken.value) // hack for web CORS restrictions
                url(WebsocketApiConfig.scheme, WebsocketApiConfig.host, DEFAULT_PORT, path)
            },
            block = {
                try {
                    coroutineScope {
                        launch { outputMessages() }
                        launch { inputMessages() }
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        )
    }

    override fun close(): Unit = wsClient.close()
}

internal expect val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*>
