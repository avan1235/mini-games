package ml.dev.kotlin.minigames.shared.ui.component.set

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.component.SetComponent
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.model.SetGameSnapshot
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.component.ProportionKeeper

@Composable
internal fun SetGamePlay(
    component: SetComponent,
    gameState: SetGameSnapshot,
    stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        GameTopBar(
            points = component.points(gameState),
            role = component.userRole(gameState),
            onClose = component::closeGame,
        )
        ProportionKeeper {
            SetBoard(
                setGame = gameState,
                onProposal = { component.emitSetProposal(it, stateMessages) }
            )
        }
    }
}
