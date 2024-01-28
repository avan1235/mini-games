package ml.dev.kotlin.minigames.shared.websocket

import ml.dev.kotlin.minigames.shared.ClientBuildConfiguration

actual object WebsocketApiConfig {
    actual val host = ClientBuildConfiguration.WEB_CLIENT_API_HOST
    actual val scheme = ClientBuildConfiguration.WEBSOCKET_CLIENT_API_SCHEME
}
