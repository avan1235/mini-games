package ml.dev.kotlin.minigames.shared.viewmodel

import android.content.Context
import android.view.Window
import android.view.WindowManager
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

actual class ViewModelContext(
        actual val keeper: InstanceKeeper,
        val androidContext: Context,
        val window: Window,
) {
    @Suppress("DEPRECATION")
    actual fun adjustResize(): Unit = window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    actual fun adjustPan(): Unit = window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}
