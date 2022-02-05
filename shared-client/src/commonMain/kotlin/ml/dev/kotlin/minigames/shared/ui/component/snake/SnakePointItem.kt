package ml.dev.kotlin.minigames.shared.ui.component.snake

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.ui.util.randomColor
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
fun SnakePointItem(
  pos: V2,
  head: V2,
  mapSize: DpSize,
  size: Dp = 16.dp,
  durationMillis: Int = 5_000,
) {
  val diff = pos - head
  val color = CACHED_COLORS[pos]
  val radius = size / 2
  val checkX = (mapSize.width - size) / 2 + diff.x.dp
  val checkY = (mapSize.height - size) / 2 + diff.y.dp

  if (checkX !in -radius..mapSize.width + radius || checkY !in -radius..mapSize.height + radius) return

  val infiniteTransition = rememberInfiniteTransition()
  val animatedSize by infiniteTransition.animateFloat(
    initialValue = size.value,
    targetValue = size.value,
    animationSpec = infiniteRepeatable(
      keyframes {
        this.durationMillis = durationMillis
        size.value * 0.8f at 0 with LinearEasing
        size.value * 1.2f at durationMillis / 2 with LinearEasing
        size.value * 0.8f at durationMillis
      }
    )
  )
  val x = (mapSize.width - animatedSize.dp) / 2 + diff.x.dp
  val y = (mapSize.height - animatedSize.dp) / 2 + diff.y.dp

  Box(
    modifier = Modifier
      .offset(x, y)
      .background(
        brush = Brush.radialGradient(
          colors = listOf(color, Color.Transparent),
          tileMode = TileMode.Mirror
        ),
        shape = CircleShape
      )
      .size(animatedSize.dp)
  )
}

private val CACHED_COLORS: ComputedMap<V2, Color> = ComputedMap { randomColor() }
