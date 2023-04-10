package ml.dev.kotlin.minigames.shared.ui.component.snake

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.SnakeGameSnapshot
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.viewmodel.SnakeGameViewModel

@Composable
internal fun SnakeGamePlay(
    navigator: Navigator<ScreenRoute>,
    vm: SnakeGameViewModel,
    gameState: SnakeGameSnapshot,
    clientMessages: MutableStateFlow<GameClientMessage?>,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val center = with(LocalDensity.current) { Offset(maxWidth.toPx() / 2, maxHeight.toPx() / 2) }
        val scope = rememberCoroutineScope()
        var lastHead by remember { mutableStateOf(V2.ZERO) }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(center) { detectDirectionChange(scope, center, vm, clientMessages) }
        ) box@{
            val head = vm.userSnake(gameState)?.head?.pos?.also { lastHead = it }
            val mapSize = DpSize(maxWidth, maxHeight)
            SnakeBackground(head ?: lastHead, mapSize)
            gameState.items.forEach { SnakePointItem(it, head ?: lastHead, mapSize) }
            gameState.snakes.forEach { Snake(it.key, it.value, head ?: lastHead, mapSize) }
            GameTopBar(
                points = vm.points(gameState),
                role = vm.userRole(gameState),
                onClose = { navigator.navigate(ScreenRoute.LogInScreen, dropAll = true) }
            )
        }
    }

}

private suspend fun PointerInputScope.detectDirectionChange(
    coroutineScope: CoroutineScope,
    center: Offset,
    vm: SnakeGameViewModel,
    clientMessages: MutableStateFlow<GameClientMessage?>,
) {
    awaitEachGesture {
        awaitFirstDown()
        do {
            val event = awaitPointerEvent()
            event.changes.fold(Offset.Zero) { acc, c -> acc + c.position - center }
                .run { V2(x, y) }.takeIf { it != V2.ZERO }
                ?.let { coroutineScope.launch { vm.emitDirectionChange(it, clientMessages) } }
            event.changes.forEach { if (it.positionChange() != Offset.Zero) it.consume() }
        } while (event.changes.any { it.pressed })
    }
}
