package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.runtime.MutableState

fun <T> T.set(vararg state: MutableState<T>) {
  state.forEach { it.value = this }
}
