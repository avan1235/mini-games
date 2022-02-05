package ml.dev.kotlin.minigames.shared.api

private const val GAME = "$API_VERSION/game"

private const val SET_GAME = "$GAME/set"
private const val SNAKE_GAME = "$GAME/snake"

val SET_GAME_WEBSOCKET: (String) -> String = { gameName -> "$SET_GAME/$gameName" }
val SNAKE_GAME_WEBSOCKET: (String) -> String = { gameName -> "$SNAKE_GAME/$gameName" }
