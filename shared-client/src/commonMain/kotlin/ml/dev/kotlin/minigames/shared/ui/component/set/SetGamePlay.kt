package ml.dev.kotlin.minigames.shared.ui.component.set

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.SetGameSnapshot
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.component.ProportionKeeper
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.viewmodel.SetGameViewModel

@Composable
internal fun SetGamePlay(
    navigator: Navigator<ScreenRoute>,
    vm: SetGameViewModel,
    gameState: SetGameSnapshot,
    clientMessages: MutableStateFlow<GameClientMessage?>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        val scope = rememberCoroutineScope()
        GameTopBar(
            points = vm.points(gameState),
            role = vm.userRole(gameState),
            onClose = { navigator.navigate(ScreenRoute.LogInScreen, dropAll = true) }
        )
        ProportionKeeper {
            SetBoard(
                setGame = gameState,
                onProposal = { scope.launch { vm.emitSetProposal(it, clientMessages) } }
            )
        }
    }
}
