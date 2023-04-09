package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.api.SET_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.Game
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient


internal class SetGameViewModel(
    context: ViewModelContext,
    gameAccessData: GameAccessData,
) : GameViewModel<SetGameSnapshot>(context, gameAccessData) {

    override val client: GameClient =
        ctx.keeper.getOrCreate(Game.Set) { GameClient(SET_GAME_WEBSOCKET) }

    suspend fun emitSetProposal(
        cardsIds: Set<Int>,
        clientMessages: MutableStateFlow<GameClientMessage?>
    ) {
        val proposal = SetProposal(cardsIds)
        val update = SetGameUpdate(proposal)
        val message = GameStateUpdateClientMessage(update, timestamp = now())
        clientMessages.emit(message)
    }
}
