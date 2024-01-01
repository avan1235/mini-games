package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.*

enum class CardType {
    Hearts, Diamonds, Clubs, Spades, Flowers, Stars,
}

private fun CardType.toPath(size: Size): Path = when (this) {
    CardType.Hearts -> drawHeartPath(size)
    CardType.Diamonds -> drawDiamondPath(size)
    CardType.Clubs -> drawClubsPath(size)
    CardType.Spades -> drawSpadesPath(size)
    CardType.Flowers -> TODO()
    CardType.Stars -> drawStarPath(size)
}

private fun drawClubsPath(size: Size): Path = Path().apply {
    val width = size.width
    val r2 = size.minDimension / 2 * 5 / 6
    val r = r2 / 2

    val circleSize = Size(r2, r2)
    val centerX = width / 2 - r
    val centerY = 0f

    fun circle(offset: Offset) = arcTo(
        rect = Rect(
            offset = offset,
            size = circleSize
        ),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 359f,
        forceMoveTo = true
    )
//    circle(Offset(centerX, centerY))
//    circle(Offset(centerX + r, centerY + r * sqrt(3f)))
//    circle(Offset(centerX - r, centerY + r * sqrt(3f)))

    moveTo(width / 2, r)
    lineTo(centerX + r, centerY + r * sqrt(3f) + r)
    lineTo(centerX - r, centerY + r * sqrt(3f) + r)
    lineTo(width / 2, r)

    close()
}

private fun drawSpadesPath(size: Size): Path = Path().apply {
    val width = size.width
    val height = size.height

    moveTo(x = width / 2, y = 4 * height / 5)

    cubicTo(
        x1 = 9 * width / 14, y1 = height,
        x2 = width, y2 = 14 * height / 15,
        x3 = 27 * width / 28, y3 = 3 * height / 5
    )

    cubicTo(
        x1 = 13 * width / 14, y1 = height / 3,
        x2 = 4 * width / 7, y2 = height / 6,
        x3 = width / 2, y3 = 0f
    )

    cubicTo(
        x1 = 3 * width / 7, y1 = height / 6,
        x2 = width / 14, y2 = height / 3,
        x3 = width / 28, y3 = 3 * height / 5
    )

    cubicTo(
        x1 = 0f, y1 = 14 * height / 15,
        x2 = 4 * width / 14, y2 = height,
        x3 = width / 2, y3 = 4 * height / 5
    )

    lineTo(
        x = width / 2 + width / 10,
        y = height,
    )

    lineTo(
        x = width / 2 - width / 10,
        y = height,
    )

    lineTo(
        x = width / 2,
        y = 4 * height / 5,
    )

    close()
}

private fun drawHeartPath(size: Size): Path = Path().apply {
    val width = size.width
    val height = size.height

    moveTo(x = width / 2, y = height / 5)

    cubicTo(
        x1 = 5 * width / 14, y1 = 0f,
        x2 = 0f, y2 = height / 15,
        x3 = width / 28, y3 = 2 * height / 5
    )

    cubicTo(
        x1 = width / 14, y1 = 2 * height / 3,
        x2 = 3 * width / 7, y2 = 5 * height / 6,
        x3 = width / 2, y3 = height
    )

    cubicTo(
        x1 = 4 * width / 7, y1 = 5 * height / 6,
        x2 = 13 * width / 14, y2 = 2 * height / 3,
        x3 = 27 * width / 28, y3 = 2 * height / 5
    )

    cubicTo(
        x1 = width, y1 = height / 15,
        x2 = 9 * width / 14, y2 = 0f,
        x3 = width / 2, y3 = height / 5
    )

    close()
}

private fun drawDiamondPath(size: Size): Path = Path().apply {
    val width = size.width
    val height = size.height
    val centerX = width / 2f

    moveTo(x = centerX, y = 0f)
    lineTo(x = 5 * width / 6, y = height / 2)
    lineTo(x = centerX, y = height)
    lineTo(x = width / 6, y = height / 2)

    close()
}

private fun drawStarPath(
    size: Size,
    numPoints: Int = 5,
): Path = Path().apply {

    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val outerRadius = min(size.width, size.height) / 2f
    val innerRadius = outerRadius / 2.5f // Adjust the inner radius as needed

    val doublePi = 2 * PI
    val angleIncrement = doublePi / numPoints

    var angle = -PI / 2f // Start angle at the top point of the star
    moveTo(
        x = (centerX + outerRadius * cos(angle)).toFloat(),
        y = (centerY + outerRadius * sin(angle)).toFloat()
    )

    for (i in 1..numPoints) {
        angle += angleIncrement / 2 // Move to the inner angle first
        lineTo(
            x = (centerX + innerRadius * cos(angle)).toFloat(),
            y = (centerY + innerRadius * sin(angle)).toFloat()
        )
        angle += angleIncrement / 2 // Move to the outer angle
        lineTo(
            x = (centerX + outerRadius * cos(angle)).toFloat(),
            y = (centerY + outerRadius * sin(angle)).toFloat()
        )
    }

    close()
}

@Composable
internal fun Card(
    cardType: CardType,
    height: Dp = 156.dp,
    width: Dp = 0.66f * height,
    cornerRadius: Dp = height / 15f,
) {
    var degrees by remember { mutableFloatStateOf(0f) }
    Canvas(
        modifier = Modifier
            .requiredSize(width, height)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    degrees += dragAmount.x
                }
            }
    ) {
        rotate(
            degrees = degrees,
            pivot = Offset(width.toPx() / 2, height.toPx())
        ) {
            drawRoundRect(
                color = Color.White,
                size = Size(width.toPx(), height.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
            val pathSize = Size(size.minDimension, size.minDimension)
            translate(
                left = (size.width - pathSize.width) / 2,
                top = (size.height - pathSize.height) / 2,
            ) {
                drawPath(cardType.toPath(pathSize), color = Color.Green)
            }
        }
    }
}