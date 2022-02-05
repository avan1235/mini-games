package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScrollScreen(
  up: @Composable BoxScope.() -> Unit,
  down: @Composable BoxScope.() -> Unit,
  icon: ImageVector? = null,
  threshold: Float = 0.3f,
  scrollIconSize: Dp = 24.dp,
  padding: Dp = 8.dp,
): Unit = with(LocalDensity.current) {
  BoxWithConstraints {
    val fullHeight = maxHeight
    val height = maxHeight - scrollIconSize - (padding * 2)
    val swipeState = rememberSwipeableState(1)
    val anchors = mapOf(0f to 0, height.toPx() to 1)
    val progress = swipeState.progress
    val rotDegrees = when {
      icon != null -> 0f
      progress.from == 1 && progress.to == 1 -> 0f
      progress.from == 1 && progress.to == 0 -> progress.fraction * 180f
      progress.from == 0 && progress.to == 1 -> (1f - progress.fraction) * 180f
      else -> 180f
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(fullHeight)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(height)
          .background(MaterialTheme.colors.surface),
        content = up
      )
      Box(
        modifier = Modifier
          .offset { IntOffset(0, swipeState.offset.value.roundToInt()) }
          .fillMaxWidth()
          .height(fullHeight)
          .background(MaterialTheme.colors.surface)
          .swipeable(
            state = swipeState,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(threshold) },
            orientation = Orientation.Vertical
          )
      ) {
        BoxWithConstraints {
          val downHeight = maxHeight
          Column(modifier = Modifier.fillMaxSize()) {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colors.background)
                  .padding(padding),
                contentAlignment = Alignment.TopCenter
              ) {
                Box(modifier = Modifier.rotate(rotDegrees)) {
                  ShadowIcon(
                    imageVector = icon ?: Icons.Default.ArrowUpward,
                    contentDescription = "scrollDown",
                    size = scrollIconSize
                  )
                }
              }
            }
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(downHeight),
              content = down
            )
          }
        }
      }
    }
  }
}
