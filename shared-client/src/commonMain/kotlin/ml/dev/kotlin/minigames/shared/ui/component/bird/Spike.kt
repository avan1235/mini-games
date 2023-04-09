package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.component.SizedCanvas
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
internal fun LeftRightSpikes(
        spikes: List<V2>,
        mapSize: DpSize,
) {
    spikes.forEach { LeftRightSpike(it, mapSize) }
}

@Composable
internal fun UpDownConstantSpikes(mapSize: DpSize, count: Int = 8) {
    val stepX = 2f / (count + 1)
    for (x in 1..count) {
        UpDownSpike(V2(-1f + stepX * x, -1f), mapSize)
        UpDownSpike(V2(-1f + stepX * x, 1f), mapSize)
    }
}

@Composable
private fun LeftRightSpike(
        pos: V2,
        mapSize: DpSize,
        size: DpSize = DpSize(20.dp, 40.dp),
) {
    Spike(pos, size, mapSize) { spikeSize ->
        val wallX = (pos.x + 1f) / 2f * spikeSize.width
        val spikeX = spikeSize.width - wallX

        moveTo(wallX, spikeSize.height * 0.5f)
        lineTo(wallX, 0f)
        lineTo(spikeX, spikeSize.height * 0.5f)
        lineTo(wallX, spikeSize.height)
        lineTo(wallX, spikeSize.height * 0.5f)
    }
}

@Composable
private fun UpDownSpike(
        pos: V2,
        mapSize: DpSize,
        size: DpSize = DpSize(40.dp, 20.dp),
) {
    Spike(pos, size, mapSize) { spikeSize ->
        val spikeY = (pos.y + 1f) / 2f * spikeSize.height
        val wallY = spikeSize.height - spikeY

        moveTo(spikeSize.width * 0.5f, wallY)
        lineTo(0f, wallY)
        lineTo(spikeSize.width * 0.5f, spikeY)
        lineTo(spikeSize.width, wallY)
        lineTo(spikeSize.width * 0.5f, wallY)
    }
}

@Composable
private fun Spike(
        pos: V2,
        spikeSize: DpSize,
        mapSize: DpSize,
        draw: Path.(size: Size) -> Unit
) {
    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .offset(
                            x = (mapSize.width - spikeSize.width) * 0.5f * (1f + pos.x),
                            y = (mapSize.height - spikeSize.height) * 0.5f * (1f - pos.y),
                    )
    ) {
        val color = MaterialTheme.colors.surface
        SizedCanvas(spikeSize.width, spikeSize.height) {
            val path = Path().apply { draw(size) }
            clipPath(
                    path = path,
                    clipOp = ClipOp.Intersect,
            ) {
                drawPath(
                        path = path,
                        color = color,
                )
                drawRect(
                        topLeft = Offset.Zero,
                        color = color,
                        size = size
                )
            }
        }
    }
}
