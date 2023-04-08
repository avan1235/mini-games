package ml.dev.kotlin.minigames.shared.websocket.client

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

internal actual val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*> = Darwin
