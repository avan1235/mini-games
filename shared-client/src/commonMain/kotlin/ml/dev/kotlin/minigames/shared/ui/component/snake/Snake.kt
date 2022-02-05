package ml.dev.kotlin.minigames.shared.ui.component.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.model.Snake
import ml.dev.kotlin.minigames.shared.model.SnakePart
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.ui.util.lightScaled
import ml.dev.kotlin.minigames.shared.ui.util.randomColor
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
fun Snake(
  username: Username,
  snake: Snake,
  head: V2,
  mapSize: DpSize,
): Unit = with(snake) {
  parts.asReversed().forEachIndexed { idx, part ->
    val degrees = when {
      idx < parts.size - 1 -> null
      v.x <= 0f -> v deg V2.ZERO_ONE
      else -> 360f - (V2.ZERO_ONE deg v)
    }
    SnakePart(part, head, mapSize, snake.size.dp, degrees, CACHED_COLORS[username])
  }
}

private val CACHED_COLORS: ComputedMap<Username, Color> = ComputedMap { randomColor() }

@Composable
private fun SnakePart(
  part: SnakePart,
  head: V2,
  mapSize: DpSize,
  size: Dp,
  degrees: Float?,
  color: Color,
  lightScale: Float = 0.5f,
) {
  val diff = part.pos - head
  val x = (mapSize.width - size) / 2 + diff.x.dp
  val y = (mapSize.height - size) / 2 + diff.y.dp
  val radius = size / 2
  if (x !in -radius..mapSize.width + radius || y !in -radius..mapSize.height + radius) return
  Box(
    modifier = Modifier
      .offset(x, y)
      .background(
        brush = Brush.radialGradient(
          colors = listOf(color, color.lightScaled(lightScale)),
          tileMode = TileMode.Mirror
        ),
        shape = CircleShape
      )
      .size(size).run {
        if (degrees != null) rotate(degrees) else this
      },
    contentAlignment = Alignment.BottomCenter
  ) {
    if (degrees != null) Row {
      SnakeEye(size)
      Spacer(modifier = Modifier.size(size / 8))
      SnakeEye(size)
    }
  }
}

@Composable
private fun SnakeEye(size: Dp) {
  Box(
    modifier = Modifier
      .size(size / 3)
      .clip(CircleShape)
      .background(Color.White),
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .size(size / 5)
        .clip(CircleShape)
        .background(Color.Black)
    )
  }
}
