package ml.dev.kotlin.minigames.shared.rest.client

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*> = Js
