package ml.dev.kotlin.minigames.shared.ui.component.snake

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
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
fun SnakeGamePlay(
  navigator: Navigator<ScreenRoute>,
  vm: SnakeGameViewModel,
  gameState: SnakeGameSnapshot,
  clientMessages: MutableStateFlow<GameClientMessage?>
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
  ) {
    val center = with(LocalDensity.current) { Offset(maxWidth.toPx() / 2, maxHeight.toPx() / 2) }
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(center) { detectDirectionChange(scope, center, vm, clientMessages) }
    ) box@{
      val head = vm.userSnake(gameState)?.head?.pos ?: return@box
      val mapSize = DpSize(maxWidth, maxHeight)
      SnakeBackground(head, mapSize)
      gameState.items.forEach { SnakePointItem(it, head, mapSize) }
      gameState.snakes.forEach { Snake(it.key, it.value, head, mapSize) }
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
  clientMessages: MutableStateFlow<GameClientMessage?>
) {
  forEachGesture {
    awaitPointerEventScope {
      awaitFirstDown()
      do {
        val event = awaitPointerEvent()
        event.changes.fold(Offset.Zero) { acc, c -> acc + c.position - center }
          .run { V2(x, y) }.takeIf { it != V2.ZERO }
          ?.let { coroutineScope.launch { vm.emitDirectionChange(it, clientMessages) } }
        event.changes.forEach { it.consumePositionChange() }
      } while (event.changes.any { it.pressed })
    }
  }
}
