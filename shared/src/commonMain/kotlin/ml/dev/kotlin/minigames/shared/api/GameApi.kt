package ml.dev.kotlin.minigames.shared.api

private const val GAME = "$API_VERSION/game"

private const val SET_GAME = "$GAME/set"
private const val SNAKE_GAME = "$GAME/snake"

val SET_GAME_WEBSOCKET: (String) -> String = { serverName -> "$SET_GAME/$serverName" }
val SNAKE_GAME_WEBSOCKET: (String) -> String = { serverName -> "$SNAKE_GAME/$serverName" }
