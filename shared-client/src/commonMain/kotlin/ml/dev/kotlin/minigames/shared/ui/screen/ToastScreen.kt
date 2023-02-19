package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ml.dev.kotlin.minigames.shared.ui.component.ToastBuffer
import ml.dev.kotlin.minigames.shared.ui.component.ToastOverlay

@Composable
fun ToastScreen(content: @Composable ToastBuffer.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        val buffer = remember { ToastBuffer(mutableStateOf(null)) }
        CompositionLocalProvider(LocalToastContext provides buffer) { content(buffer) }
        ToastOverlay(buffer.state)
    }
}

val LocalToastContext: ProvidableCompositionLocal<ToastBuffer?> = staticCompositionLocalOf { null }
