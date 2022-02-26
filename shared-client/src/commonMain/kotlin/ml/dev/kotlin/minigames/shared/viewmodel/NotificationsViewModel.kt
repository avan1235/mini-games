package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class NotificationsViewModel(context: ViewModelContext) : ViewModel(context) {

  private val _notifications: SnapshotStateList<String> = mutableStateListOf()
  val notifications: List<String> get() = _notifications

  fun addNotification(message: String) {
    _notifications.add(message)
  }
}
