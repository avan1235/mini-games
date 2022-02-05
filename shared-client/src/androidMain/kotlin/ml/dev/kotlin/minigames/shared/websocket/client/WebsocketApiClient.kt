package ml.dev.kotlin.minigames.shared.websocket.client

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

internal actual val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*> = OkHttp
