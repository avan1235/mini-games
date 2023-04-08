package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class NotificationsViewModel(context: ViewModelContext) : ViewModel(context) {

    private var _notifications: SnapshotStateList<IndexedNotification> = mutableStateListOf()
    val notifications: List<IndexedNotification> get() = _notifications

    fun addNotification(message: String) {
        NOTIFICATION_IDX += 1
        val notification = IndexedNotification(message, NOTIFICATION_IDX)
        _notifications.add(notification)
    }

    fun removeNotification(notification: IndexedNotification) {
        _notifications.remove(notification)
    }
}

data class IndexedNotification(val message: String, val idx: Int)

private var NOTIFICATION_IDX: Int = 0
