package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.api.BIRD_GAME_WEBSOCKET
import ml.dev.kotlin.minigames.shared.model.BirdGameSnapshot
import ml.dev.kotlin.minigames.shared.model.BirdGameUpdate
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient

interface BirdComponent : GameComponent<BirdGameSnapshot> {

    fun emitFly(stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>)
}

internal class BirdComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    gameAccessData: GameAccessData,
    onCloseGame: (String?) -> Unit,
) : AbstractGameComponent<BirdGameSnapshot>(
    appContext,
    componentContext,
    gameAccessData,
    onCloseGame
), BirdComponent {

    override val client: GameClient = GameClient(BIRD_GAME_WEBSOCKET)

    override fun emitFly(stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>) {
        scope.launch {
            val message = GameStateUpdateClientMessage(BirdGameUpdate, timestamp = now())
            stateMessages.emit(message)
        }
    }
}