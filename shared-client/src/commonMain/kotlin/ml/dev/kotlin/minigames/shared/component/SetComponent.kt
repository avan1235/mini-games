package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.api.SET_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.model.SetGameSnapshot
import ml.dev.kotlin.minigames.shared.model.SetGameUpdate
import ml.dev.kotlin.minigames.shared.model.SetProposal
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient

interface SetComponent : GameComponent<SetGameSnapshot> {
    fun emitSetProposal(
        cardsIds: Set<Int>,
        stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    )
}

internal class SetComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    gameAccessData: GameAccessData,
    onCloseGame: (String?) -> Unit,
) : AbstractGameComponent<SetGameSnapshot>(
    appContext,
    componentContext,
    gameAccessData,
    onCloseGame
), SetComponent {

    override val client: GameClient = GameClient(SET_GAME_WEBSOCKET)

    override fun emitSetProposal(
        cardsIds: Set<Int>,
        stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    ) {
        scope.launch {
            val proposal = SetProposal(cardsIds)
            val update = SetGameUpdate(proposal)
            val message = GameStateUpdateClientMessage(update, timestamp = now())
            stateMessages.emit(message)
        }
    }
}