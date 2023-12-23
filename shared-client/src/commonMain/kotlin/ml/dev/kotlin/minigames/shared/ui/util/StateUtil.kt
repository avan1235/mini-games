package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.MutableStateFlow

fun <T> T.set(vararg state: MutableState<T>) {
    state.forEach { it.value = this }
}

fun <T> T.set(vararg state: MutableStateFlow<T>) {
    state.forEach { it.value = this }
}
