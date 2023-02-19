package ml.dev.kotlin.minigames.shared.websocket

import ml.dev.kotlin.minigames.shared.BuildConfiguration

actual object WebsocketApiConfig {
    actual val host = BuildConfiguration.ANDROID_CLIENT_API_HOST
    actual val scheme = BuildConfiguration.WEBSOCKET_CLIENT_API_SCHEME
}
