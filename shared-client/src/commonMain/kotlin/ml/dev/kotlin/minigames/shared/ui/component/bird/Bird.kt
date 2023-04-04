package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.ui.component.SizedCanvas
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.V2


@Composable
fun Bird(
    pos: V2,
    direction: BirdNozzleDirection,
    mapSize: DpSize,
    isAlive: Boolean,
    theme: BirdTheme,
    birdSize: Dp = 32.dp
) {
    Positioned(pos, DpSize(birdSize, birdSize), mapSize) {
        Box(
            modifier = Modifier
                .size(birdSize)
                .background(theme.bodyColor, theme.bodyShape)
        ) {
            BirdWing(birdSize, theme.wingColor, isAlive, fullFill)
            BirdNozzle(birdSize, theme.nozzleColor, direction, isAlive, fullFill)
        }
    }
}

private const val BIRD_NOZZLE_DURATION_MILLIS: Int = 7 * 3 * 3 * 3 * 3

enum class BirdNozzleDirection(val m: Float) {
    Right(1f), Left(-1f);

    companion object {
        fun fromVelocity(v: V2): BirdNozzleDirection =
            if (v.x > 0f) Right else Left
    }
}

@Composable
private fun BirdNozzle(
    birdSize: Dp,
    color: Color,
    direction: BirdNozzleDirection,
    isAlive: Boolean,
    fill: DrawScope.(color: Color) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val nozzleSpace = AnimateInfiniteRepeatableReverse(
        animation = tween(
            durationMillis = BIRD_NOZZLE_DURATION_MILLIS,
            easing = CubicBezierEasing(1.0f, 0.2f, 0.0f, 0.4f),
        )
    )
    if (!isAlive) {
        scope.launch { nozzleSpace.snapTo(1f) }
    }
    SizedCanvas(birdSize, birdSize) {
        val path = Path().apply {
            moveTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.25f)
            lineTo(
                size.width * (0.5f + direction.m * 1f),
                size.height * 0.25f + size.height * 0.23f * nozzleSpace.value
            )
            lineTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.5f)
            lineTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.25f)
            moveTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.5f)
            lineTo(
                size.width * (0.5f + direction.m * 0.7f),
                size.height * 0.75f - size.height * 0.23f * nozzleSpace.value
            )
            lineTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.75f)
            lineTo(size.width * (0.5f + direction.m * 0.5f), size.height * 0.5f)
        }
        clipPath(
            path = path,
            clipOp = ClipOp.Intersect
        ) {
            drawPath(
                path = path,
                color = color,
            )
            fill(color)
        }
    }
}

private const val BIRD_WING_DURATION_MILLIS: Int = 5 * 5 * 2 * 5 * 2

@Composable
private fun BirdWing(
    birdSize: Dp,
    color: Color,
    isAlive: Boolean,
    fill: DrawScope.(color: Color) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val wingHeight = AnimateInfiniteRepeatableReverse(
        animation = tween(
            durationMillis = BIRD_WING_DURATION_MILLIS,
            easing = CubicBezierEasing(1.0f, 0.2f, 0.0f, 0.4f),
        )
    )
    if (!isAlive) {
        scope.launch { wingHeight.snapTo(1f) }
    }
    SizedCanvas(birdSize, birdSize) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.8f * wingHeight.value + size.height * 0.1f)
            lineTo(size.width * 0.75f, size.height * 0.5f)
            lineTo(size.width * 0.25f, size.height * 0.5f)
        }
        clipPath(
            path = path,
            clipOp = ClipOp.Intersect
        ) {
            drawPath(
                path = path,
                color = color,
            )
            fill(color)
        }
    }
}

private val fullFill: DrawScope.(Color) -> Unit = { color ->
    drawRect(
        topLeft = Offset.Zero,
        color = color,
        size = Size(size.width, size.height)
    )
}

@Composable
private fun AnimateInfiniteRepeatableReverse(
    animation: DurationBasedAnimationSpec<Float>,
    targetValue: Float = 1f,
): Animatable<Float, AnimationVector1D> {
    val value = remember { Animatable(0f) }
    LaunchedEffect(value) {
        value.animateTo(
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = animation,
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    return value
}
