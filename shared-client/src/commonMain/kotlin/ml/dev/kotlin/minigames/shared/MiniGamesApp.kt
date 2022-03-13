package ml.dev.kotlin.minigames.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ml.dev.kotlin.minigames.shared.ui.MiniGamesRoutes
import ml.dev.kotlin.minigames.shared.ui.screen.ToastScreen
import ml.dev.kotlin.minigames.shared.ui.theme.Theme
import ml.dev.kotlin.minigames.shared.ui.util.lightScaled
import ml.dev.kotlin.minigames.shared.util.initClientLogger
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext
import kotlin.math.roundToInt

@Composable
fun MiniGamesApp(context: ViewModelContext) {
  initClientLogger()

  Theme {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.surface)
    ) {
      ToastScreen { MiniGamesRoutes(context) }
//      WIP()
    }
  }
}

@Composable
fun WIP() {
  HockeyPlayer(size = 0.2f)
}

@Composable
fun HockeyPlayer(
  size: Float,
  onXChange: (Float) -> Unit = {},
  onYChange: (Float) -> Unit = {},
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    fun Float.coerceIn(from: Dp, to: Dp): Float = with(density) { coerceIn(from.toPx(), to.toPx()) }

    val dpSize = with(density) { min(maxHeight, maxWidth) * size }

    val width = with(density) { maxWidth.toPx() }
    val height = with(density) { maxHeight.toPx() }

    HockeyPlayerPuck(
      modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(dpSize)
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consumeAllChanges()
            offsetX = (offsetX + dragAmount.x).run { coerceIn(0.dp, maxWidth - dpSize) }
            offsetY = (offsetY + dragAmount.y).run { coerceIn(0.dp, maxHeight - dpSize) }
          }
        }
    )
  }
}

@Composable
fun HockeyPlayerPuck(
  modifier: Modifier = Modifier,
  color: Color = Color.Red,
) {
  Canvas(modifier = modifier) {
    val inner = Path().apply { addOval(Rect(center, size.minDimension / 2.5f)) }
    clipPath(
      path = inner,
      clipOp = ClipOp.Difference
    ) {
      drawPath(
        path = Path().apply { addOval(Rect(center, size.minDimension / 2)) },
        brush = Brush.radialGradient(
          colors = listOf(
            Color.Transparent,
            color,
            color.lightScaled(1.2f),
            color,
            Color.Transparent
          )
        ),
      )
    }
    drawPath(
      path = inner,
      brush = Brush.radialGradient(colors = listOf(Color.Transparent, color, color.lightScaled(1.2f))),
    )
  }
}
