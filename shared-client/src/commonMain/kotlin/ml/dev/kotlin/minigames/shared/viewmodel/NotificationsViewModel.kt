package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.concurrent.atomic.AtomicInteger

class NotificationsViewModel(context: ViewModelContext) : ViewModel(context) {

  private var _notifications: SnapshotStateList<IndexedNotification> = mutableStateListOf()
  val notifications: List<IndexedNotification> get() = _notifications

  fun addNotification(message: String) {
    val notification = IndexedNotification(message, NOTIFICATION_IDX.getAndIncrement())
    _notifications.add(notification)
  }

  fun removeNotification(notification: IndexedNotification) {
    _notifications.remove(notification)
  }
}

data class IndexedNotification(val message: String, val idx: Int)

private val NOTIFICATION_IDX: AtomicInteger = AtomicInteger(0)
