package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.api.SNAKE_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.Game
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient


internal class SnakeGameViewModel(
    context: ViewModelContext,
    gameAccessData: GameAccessData,
) : GameViewModel<SnakeGameSnapshot>(context, gameAccessData) {

    override val client: GameClient =
        ctx.keeper.getOrCreate(Game.SnakeIO) { GameClient(SNAKE_GAME_WEBSOCKET) }

    suspend fun emitDirectionChange(dir: V2, clientMessages: MutableStateFlow<GameClientMessage?>) {
        val direction = SnakeDirection(dir)
        val update = SnakeGameUpdate(direction)
        val message = GameStateUpdateClientMessage(update, timestamp = now())
        clientMessages.emit(message)
    }

    fun userSnake(snapshot: SnakeGameSnapshot): Snake? = snapshot.snakes[username]
}
