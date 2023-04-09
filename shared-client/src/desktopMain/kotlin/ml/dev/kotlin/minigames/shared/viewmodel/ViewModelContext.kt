package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.InstanceKeeper

actual class ViewModelContext(
        actual val keeper: InstanceKeeper,
) {
    actual fun adjustResize(): Unit = Unit
    actual fun adjustPan(): Unit = Unit
}

