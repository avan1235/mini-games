package ml.dev.kotlin.minigames.shared.api

private const val GAME = "$API_VERSION/game"

private const val SET_GAME = "$GAME/set"
private const val SNAKE_GAME = "$GAME/snake"
private const val BIRD_GAME = "$GAME/bird"

class GamePath(
    gamePath: String,
    serverName: String,
) {
    val dataPath: String = "$gamePath/$serverName/data"
    val statePath: String = "$gamePath/$serverName/state"
}

val SET_GAME_WEBSOCKET: (String) -> GamePath = { serverName -> GamePath(SET_GAME, serverName) }
val SNAKE_GAME_WEBSOCKET: (String) -> GamePath = { serverName -> GamePath(SNAKE_GAME, serverName) }
val BIRD_GAME_WEBSOCKET: (String) -> GamePath = { serverName -> GamePath(BIRD_GAME, serverName) }
