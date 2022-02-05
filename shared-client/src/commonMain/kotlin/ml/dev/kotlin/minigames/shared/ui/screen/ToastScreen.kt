package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ml.dev.kotlin.minigames.shared.ui.component.ToastBuffer
import ml.dev.kotlin.minigames.shared.ui.component.ToastMessage
import ml.dev.kotlin.minigames.shared.ui.component.ToastOverlay

@Composable
fun ToastScreen(content: @Composable ToastBuffer.() -> Unit) {
  Box(modifier = Modifier.fillMaxSize()) {
    val state = remember { mutableStateOf<ToastMessage?>(null) }
    val buffer = ToastBuffer(state)
    content(buffer)
    ToastOverlay(state)
  }
}
