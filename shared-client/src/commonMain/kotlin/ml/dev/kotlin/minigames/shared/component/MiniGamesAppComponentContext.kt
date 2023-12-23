package ml.dev.kotlin.minigames.shared.component

import androidx.compose.material3.SnackbarHostState

expect class MiniGamesAppComponentContext {
    fun adjustResize()
    fun adjustPan()

    val snackbarHostState: SnackbarHostState
}