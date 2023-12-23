package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.api.SNAKE_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient

interface SnakeComponent : GameComponent<SnakeGameSnapshot> {
    fun emitDirectionChange(
        dir: V2,
        stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    )

    fun userSnake(snapshot: SnakeGameSnapshot): Snake?
}

internal class SnakeComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    gameAccessData: GameAccessData,
    onCloseGame: (String?) -> Unit,
) : AbstractGameComponent<SnakeGameSnapshot>(
    appContext,
    componentContext,
    gameAccessData,
    onCloseGame,
), SnakeComponent {

    override val client: GameClient = GameClient(SNAKE_GAME_WEBSOCKET)

    override fun emitDirectionChange(
        dir: V2,
        stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    ) {
        scope.launch {
            val direction = SnakeDirection(dir)
            val update = SnakeGameUpdate(direction)
            val message = GameStateUpdateClientMessage(update, timestamp = now())
            stateMessages.emit(message)
        }
    }

    override fun userSnake(snapshot: SnakeGameSnapshot): Snake? = snapshot.snakes[username]
}