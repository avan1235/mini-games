package ml.dev.kotlin.minigames.shared.websocket.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.JwtToken
import ml.dev.kotlin.minigames.shared.websocket.WebsocketApiConfig

class WebsocketApiClient : Closeable {

  private val wsClient = HttpClient(CLIENT_ENGINE_FACTORY) {
    install(JsonFeature) {
      serializer = KotlinxSerializer()
    }
    followRedirects = true
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
