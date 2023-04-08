package ml.dev.kotlin.minigames.shared.rest

import ml.dev.kotlin.minigames.shared.BuildConfiguration

actual object RestApiConfig {
    actual val host = BuildConfiguration.IOS_CLIENT_API_HOST
    actual val scheme = BuildConfiguration.REST_CLIENT_API_SCHEME
}
