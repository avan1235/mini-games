package ml.dev.kotlin.minigames.shared.component

import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.compose.material3.SnackbarHostState

actual class MiniGamesAppComponentContext(
    val applicationContext: Context,
    val window: Window,
) {
    @Suppress("DEPRECATION")
    actual fun adjustResize(): Unit = window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    actual fun adjustPan(): Unit = window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    actual val snackbarHostState = SnackbarHostState()
}