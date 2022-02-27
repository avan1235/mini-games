package ml.dev.kotlin.minigames.shared.viewmodel

import com.arkivanov.essenty.instancekeeper.InstanceKeeper

expect class ViewModelContext {
  val keeper: InstanceKeeper

  fun adjustResize()
  fun adjustPan()
}
