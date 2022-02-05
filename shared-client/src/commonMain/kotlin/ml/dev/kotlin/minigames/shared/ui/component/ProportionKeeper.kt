package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProportionKeeper(
  maxWidthToHeight: Float = 0.5f,
  content: @Composable BoxScope.() -> Unit
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    val proportion = maxWidth.value / maxHeight.value
    val innerWidth = if (proportion > maxWidthToHeight) maxHeight * maxWidthToHeight else maxWidth
    Box(modifier = Modifier.size(innerWidth, maxHeight), content = content)
  }
}
