package ml.dev.kotlin.minigames.shared.ui.component.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.V2
import kotlin.math.*

@Composable
fun SnakeBackground(
    head: V2,
    mapSize: DpSize,
    itemSize: Dp = 72.dp
) {
    val xItems = ceil(mapSize.width / itemSize).roundToInt() * 2 / 3
    val yItems = ceil(mapSize.height / itemSize).roundToInt()
    val xOffset = head.x % (itemSize * 3 / 2).value
    val yOffset = head.y % (itemSize * SQRT3 / 2).value
    Box(
        modifier = Modifier
            .offset(-xOffset.dp, -yOffset.dp)
            .size(mapSize.width, mapSize.height)
            .background(MaterialTheme.colors.surface)
    ) {
        for (y in -2..yItems + 2) for (x in -2..xItems + 2) {
            Box(
                modifier = Modifier.offset(
                    itemSize * x * 3 / 2,
                    itemSize * y * SQRT3 / 2
                )
            ) { SnakeBackgroundItem(itemSize) }
            Box(
                modifier = Modifier.offset(
                    itemSize * x * 3 / 2 + itemSize * 3 / 4,
                    itemSize * y * SQRT3 / 2 + itemSize * SQRT3 / 4
                )
            ) {
                SnakeBackgroundItem(itemSize)
            }
        }
    }
}

@Composable
private fun SnakeBackgroundItem(
    itemSize: Dp,
    padding: Dp = 4.dp
) {
    val color = MaterialTheme.colors.background
    val colors = listOf(color.copy(alpha = 1f), color.copy(alpha = 0.8f))
    Box(
        modifier = Modifier.size(itemSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(itemSize - padding * 2),
        ) {
            val path = Path()
            val radius = min(size.height, size.width) / 2
            BACKGROUND_ANGLES.forEachIndexed { i, (cos, sin) ->
                val x = size.width / 2 + radius * cos
                val y = size.height / 2 + radius * sin
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(colors),
            )
        }
    }
}

private val BACKGROUND_ANGLES: List<Pair<Float, Float>> =
    List(6) { Pair(cos(PI / 3 * it).toFloat(), sin(PI / 3 * it).toFloat()) }

private val SQRT3: Float = sqrt(3f)
