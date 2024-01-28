package ml.dev.kotlin.minigames.shared.rest

import ml.dev.kotlin.minigames.shared.ClientBuildConfiguration

actual object RestApiConfig {
    actual val host = ClientBuildConfiguration.WEB_CLIENT_API_HOST
    actual val scheme = ClientBuildConfiguration.REST_CLIENT_API_SCHEME
}
