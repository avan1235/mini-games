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
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.component.SnakeComponent
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.model.SnakeGameSnapshot
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
internal fun SnakeGamePlay(
    component: SnakeComponent,
    gameState: SnakeGameSnapshot,
    stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val center = with(LocalDensity.current) { Offset(maxWidth.toPx() / 2, maxHeight.toPx() / 2) }
        var lastHead by remember { mutableStateOf(V2.ZERO) }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(center) { detectDirectionChange(center, component, stateMessages) }
        ) box@{
            val head = component.userSnake(gameState)?.head?.pos?.also { lastHead = it }
            val mapSize = DpSize(maxWidth, maxHeight)
            SnakeBackground(head ?: lastHead, mapSize)
            gameState.items.forEach { SnakePointItem(it, head ?: lastHead, mapSize) }
            gameState.snakes.forEach { Snake(it.key, it.value, head ?: lastHead, mapSize) }
            GameTopBar(
                points = component.points(gameState),
                role = component.userRole(gameState),
                onClose = component::closeGame
            )
        }
    }

}

private suspend fun PointerInputScope.detectDirectionChange(
    center: Offset,
    component: SnakeComponent,
    stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
) {
    awaitEachGesture {
        awaitFirstDown()
        do {
            val event = awaitPointerEvent()
            event.changes.fold(Offset.Zero) { acc, c -> acc + c.position - center }
                .run { V2(x, y) }.takeIf { it != V2.ZERO }
                ?.let { component.emitDirectionChange(it, stateMessages) }
            event.changes.forEach { if (it.positionChange() != Offset.Zero) it.consume() }
        } while (event.changes.any { it.pressed })
    }
}
