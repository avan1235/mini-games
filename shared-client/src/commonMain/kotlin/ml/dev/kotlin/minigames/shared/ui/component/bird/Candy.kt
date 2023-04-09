package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.component.SizedCanvas
import ml.dev.kotlin.minigames.shared.ui.component.bird.CandySide.Left
import ml.dev.kotlin.minigames.shared.ui.component.bird.CandySide.Right
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.ui.util.lightScaled
import ml.dev.kotlin.minigames.shared.ui.util.randomColor
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.random

@Composable
internal fun Candy(
        pos: V2,
        mapSize: DpSize,
        candySize: DpSize = DpSize(40.dp, 20.dp),
) {
    val yAnimation by rememberInfiniteFloatUpDownTransition()
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .offset(
                            x = (mapSize.width - candySize.width) * 0.5f * (1f + pos.x),
                            y = (mapSize.height - candySize.height) * 0.5f * (1f - pos.y + yAnimation),
                    )
    ) {
        val color = CACHED_COLORS[pos]
        SizedCanvas(
                width = candySize.width,
                height = candySize.height,
                modifier = Modifier.rotate(CACHED_DEGREES[pos])
        ) {
            drawSide(Left, color)
            drawSide(Right, color)
            drawCircle(color)
        }
    }
}

private val CACHED_COLORS: ComputedMap<V2, Color> = ComputedMap { randomColor() }
private val CACHED_DEGREES: ComputedMap<V2, Float> = ComputedMap { (0f..180f).random() }

private enum class CandySide {
    Left, Right
}

private val PARTS_SCALES: FloatArray = floatArrayOf(1.0f, 0.8f)

private fun DrawScope.drawSide(
        side: CandySide,
        color: Color,
) {
    for (scale in PARTS_SCALES) {
        val path = Path().apply {
            val sideX = when (side) {
                Left -> 0f
                Right -> size.width
            }
            val spike = size.width * 0.5f

            moveTo(sideX, size.height * 0.5f)
            lineTo(sideX, size.height * 0.5f * (1f - scale))
            lineTo(spike, size.height * 0.5f)
            lineTo(sideX, size.height * 0.5f * (1f + scale))
            lineTo(sideX, size.height * 0.5f)
        }
        clipPath(
                path = path,
                clipOp = ClipOp.Intersect,
        ) {
            val lighterColor = color.lightScaled(scale)
            drawPath(
                    path = path,
                    color = lighterColor,
            )
            drawRect(
                    topLeft = Offset.Zero,
                    color = lighterColor,
                    size = size
            )
        }
    }
}

@Composable
private fun rememberInfiniteFloatUpDownTransition(
        delayMillis: Int = 300,
        maxOffset: Float = 0.02f,
): State<Float> {
    return rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = delayMillis * 4
                        0f at 0 with LinearEasing
                        maxOffset at delayMillis with LinearEasing
                        0f at delayMillis * 2 with LinearEasing
                        -maxOffset at delayMillis * 3 with LinearEasing
                        0f at delayMillis * 4 with LinearEasing
                    }
            )
    )
}
