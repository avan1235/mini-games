package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun DotsTyping(
        dotSize: Dp = 16.dp,
        spaceSize: Dp = 8.dp,
        delayMillis: Int = 300,
        maxOffset: Float = 10f,
) {
    @Composable
    fun Dot(offset: Float) {
        Spacer(
                Modifier
                        .size(dotSize)
                        .offset(y = -offset.dp)
                        .background(
                                color = MaterialTheme.colors.primary,
                                shape = CircleShape
                        )
                        .shadow(dotSize)
        )
    }

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateOffsetWithDelay(delay: Int): State<Float> = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = delayMillis * 4
                        0f at delay with LinearEasing
                        maxOffset at delay + delayMillis with LinearEasing
                        0f at delay + delayMillis * 2
                    }
            )
    )

    val offset1 by animateOffsetWithDelay(0)
    val offset2 by animateOffsetWithDelay(delayMillis)
    val offset3 by animateOffsetWithDelay(delayMillis * 2)

    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = maxOffset.dp)
    ) {
        Dot(offset1)
        Spacer(Modifier.width(spaceSize))
        Dot(offset2)
        Spacer(Modifier.width(spaceSize))
        Dot(offset3)
    }
}
