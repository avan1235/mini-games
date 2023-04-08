package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp

@Composable
internal fun SizedCanvas(width: Dp, height: Dp, modifier: Modifier = Modifier, onDraw: DrawScope.() -> Unit) {
    Canvas(
        modifier = Modifier
            .height(height)
            .width(width)
            .then(modifier),
        onDraw
    )
}