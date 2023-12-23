package ml.dev.kotlin.minigames.shared.component

import androidx.compose.material3.SnackbarHostState

actual class MiniGamesAppComponentContext {
    actual fun adjustResize() = Unit
    actual fun adjustPan() = Unit
    actual val snackbarHostState = SnackbarHostState()
}