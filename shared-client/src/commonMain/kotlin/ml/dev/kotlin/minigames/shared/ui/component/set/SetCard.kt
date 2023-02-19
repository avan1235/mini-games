package ml.dev.kotlin.minigames.shared.ui.component.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.theme.*


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetCard(
    setCardData: SetCard,
    selected: Boolean,
    width: Dp,
    height: Dp,
    padding: Dp = 6.dp,
    elevation: Dp = 4.dp,
    onClick: () -> Unit
) {
    val count = when (setCardData.count) {
        CardCount.One -> 1
        CardCount.Two -> 2
        CardCount.Three -> 3
    }
    val color = when (setCardData.color) {
        CardColor.Green -> GreenColor
        CardColor.Purple -> PurpleColor
        CardColor.Pink -> PinkColor
    }
    val fill = when (setCardData.fill) {
        CardFill.Full -> fullFill
        CardFill.Part -> partFill
        CardFill.None -> noneFill
    }
    val innerWidth = width - elevation * 2
    val innerHeight = height - elevation * 2
    val elementWidth = innerWidth - padding * 2
    val elementHeight = (innerHeight - padding * 4) / 3

    Box(
        modifier = Modifier.size(width, height),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(innerWidth, innerHeight),
            elevation = elevation,
            shape = Shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(count) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (setCardData.shape) {
                            CardShape.Diamond -> Diamond(elementWidth, elementHeight, color, fill)
                            CardShape.Oval -> Oval(elementWidth, elementHeight, color, fill)
                            CardShape.Squiggle -> Squiggle(elementWidth, elementHeight, color, fill)
                        }
                    }
                    if (it < count - 1) {
                        Spacer(modifier = Modifier.height(padding))
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(min(innerWidth, innerHeight) / 5)
                        .clip(CircleShape)
                        .background(BlueColor)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "check", tint = Color.White)
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(padding)
                .size(innerWidth, innerHeight)
                .clip(Shapes.large)
                .clickable { onClick() }
        )
    }
}

@Composable
private fun Diamond(
    width: Dp,
    height: Dp,
    color: Color,
    fill: DrawScope.(color: Color) -> Unit,
) {
    CardObjectCanvas(width, height) {
        val path = Path()
        path.moveTo(0f, size.height / 2)
        path.lineTo(size.width / 2, 0f)
        path.lineTo(size.width, size.height / 2)
        path.lineTo(size.width / 2, size.height)
        path.lineTo(0f, size.height / 2)

        clipPath(
            path = path,
            clipOp = ClipOp.Intersect
        ) {
            drawPath(
                path = path,
                color = color,
                style = stroke(width, height, this),
            )
            fill(color)
        }
    }
}

@Composable
private fun Oval(
    width: Dp,
    height: Dp,
    color: Color,
    fill: DrawScope.(color: Color) -> Unit,
) {
    CardObjectCanvas(width, height) {
        val path = Path()
        path.arcTo(
            rect = Rect(
                0f, 0f, size.width / 2, size.height
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false,
        )

        path.lineTo(size.width - size.height / 2, 0f)
        path.arcTo(
            rect = Rect(size.width - size.height, 0f, size.width, size.height),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false,
        )
        path.lineTo(
            size.height / 2, size.height
        )

        clipPath(
            path = path,
            clipOp = ClipOp.Intersect
        ) {
            drawPath(
                path = path,
                color = color,
                style = stroke(width, height, this)
            )
            fill(color)
        }
    }
}

@Composable
private fun Squiggle(
    width: Dp,
    height: Dp,
    color: Color,
    fill: DrawScope.(color: Color) -> Unit,
) {
    CardObjectCanvas(width, height) {
        val path = Path()
        path.moveTo(
            size.width * 0.936f,
            size.height * 0.27f
        )
        path.cubicTo(
            size.width * 1.0116f, size.height * 0.6642f,
            size.width * 0.8073f, size.height * 1.0944f,
            size.width * 0.567f, size.height * 0.972f
        )
        path.cubicTo(
            size.width * 0.4707f, size.height * 0.9234f,
            size.width * 0.3798f, size.height * 0.756f,
            size.width * 0.243f, size.height * 0.954f
        )
        path.cubicTo(
            size.width * 0.0864f, size.height * 1.1808f,
            size.width * 0.0486f, size.height * 1.0493999999999999f,
            size.width * 0.045f, size.height * 0.72f
        )
        path.cubicTo(
            size.width * 0.0414f, size.height * 0.396f,
            size.width * 0.1719f, size.height * 0.1746f,
            size.width * 0.324f, size.height * 0.216f
        )
        path.cubicTo(
            size.width * 0.5328f, size.height * 0.2736f,
            size.width * 0.5571f, size.height * 0.567f,
            size.width * 0.801f, size.height * 0.252f
        )
        path.cubicTo(
            size.width * 0.8577f, size.height * 0.18f,
            size.width * 0.9081f, size.height * 0.1242f,
            size.width * 0.936f, size.height * 0.27f
        )
        path.translate(Offset(0f, -size.height * 0.123f))

        clipPath(
            path = path,
            clipOp = ClipOp.Intersect
        ) {
            drawPath(
                path = path,
                color = color,
                style = stroke(width, height, this)
            )
            fill(color)
        }
    }
}

private val partFill: DrawScope.(Color) -> Unit = { color ->
    val count = 24
    repeat(count) {
        drawRect(
            topLeft = Offset(it * (1f / (count + 0.5f)) * size.width, 0f),
            color = color,
            size = Size(
                1f / (count + 0.5f) / 2 * size.width,
                size.height
            )
        )
    }
}

private val fullFill: DrawScope.(Color) -> Unit = { color ->
    drawRect(
        topLeft = Offset.Zero,
        color = color,
        size = Size(size.width, size.height)
    )
}

private val noneFill: DrawScope.(Color) -> Unit = { }

@Composable
private fun CardObjectCanvas(width: Dp, height: Dp, onDraw: DrawScope.() -> Unit) {
    Canvas(
        modifier = Modifier
            .height(height)
            .width(width),
        onDraw
    )
}

private fun stroke(width: Dp, height: Dp, density: Density): DrawStyle = with(density) {
    Stroke(width = (min(width, height) / 10).toPx() + 1f)
}
