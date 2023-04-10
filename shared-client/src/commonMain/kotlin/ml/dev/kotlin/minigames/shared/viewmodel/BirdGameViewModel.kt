package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.api.BIRD_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.BirdGameSnapshot
import ml.dev.kotlin.minigames.shared.model.BirdGameUpdate
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.ui.Game
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient


internal class BirdGameViewModel(
    context: ViewModelContext,
    gameAccessData: GameAccessData,
) : GameViewModel<BirdGameSnapshot>(context, gameAccessData) {

    override val client: GameClient =
        ctx.keeper.getOrCreate(Game.Bird) { GameClient(BIRD_GAME_WEBSOCKET) }

    suspend fun emitFly(stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>) {
        val message = GameStateUpdateClientMessage(BirdGameUpdate, timestamp = now())
        stateMessages.emit(message)
    }
}
