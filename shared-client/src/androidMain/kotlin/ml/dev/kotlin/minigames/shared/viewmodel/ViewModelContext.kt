package ml.dev.kotlin.minigames.shared.viewmodel

import android.content.Context
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

actual class ViewModelContext(
  actual val keeper: InstanceKeeper,
  val androidContext: Context
)
