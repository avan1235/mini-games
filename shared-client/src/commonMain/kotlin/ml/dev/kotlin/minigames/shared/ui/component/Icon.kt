package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun ShadowIcon(
        imageVector: ImageVector,
        contentDescription: String,
        size: Dp,
        elevation: Dp = 1.dp
) {
    Box(modifier = Modifier.size(size + elevation)) {
        Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier
                        .size(size)
                        .offset(
                                x = elevation,
                                y = elevation
                        )
                        .alpha(0.5f),
                tint = Color.Black,
        )
        Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(size),
        )
    }
}
