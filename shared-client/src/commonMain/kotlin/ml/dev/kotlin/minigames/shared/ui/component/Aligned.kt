package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Aligned(contentAlignment: Alignment, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = contentAlignment, content = content)
}
