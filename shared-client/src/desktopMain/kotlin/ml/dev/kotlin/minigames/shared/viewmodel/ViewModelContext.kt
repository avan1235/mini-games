package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

actual class ViewModelContext(
  actual val keeper: InstanceKeeper,
)

