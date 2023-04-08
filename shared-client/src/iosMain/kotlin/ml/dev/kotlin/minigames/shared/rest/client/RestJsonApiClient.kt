package ml.dev.kotlin.minigames.shared.rest.client

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.Darwin

internal actual val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*> = Darwin
