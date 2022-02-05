package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.InstanceKeeper

abstract class ViewModel(val ctx: ViewModelContext)

expect class ViewModelContext {
  val keeper: InstanceKeeper
}

