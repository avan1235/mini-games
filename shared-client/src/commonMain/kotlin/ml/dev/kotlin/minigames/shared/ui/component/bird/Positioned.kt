package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
internal fun Positioned(
        pos: V2,
        size: DpSize,
        mapSize: DpSize,
        content: @Composable BoxScope.() -> Unit
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .offset(
                            x = (mapSize.width - size.width) * 0.5f * (1f + pos.x),
                            y = (mapSize.height - size.height) * 0.5f * (1f - pos.y),
                    ),
            content = content
    )
}