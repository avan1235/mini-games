package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen

internal class NotificationsViewModel(
    context: ViewModelContext,
    countPredicate: () -> Boolean,
) : CountingViewModel<SelectedScreen>(
    ctx = context,
    countPredicate = countPredicate,
) {

    private var _notifications: SnapshotStateList<IndexedNotification> = mutableStateListOf()
    val notifications: List<IndexedNotification> get() = _notifications

    fun addNotification(message: String) {
        countNew()
        val idx = NOTIFICATION_IDX.getAndAdd(1)
        val notification = IndexedNotification(message, idx)
        _notifications.add(notification)
    }

    fun removeNotification(notification: IndexedNotification) {
        _notifications.remove(notification)
    }
}

data class IndexedNotification(val message: String, val idx: Int)

private val NOTIFICATION_IDX: AtomicInt = atomic(0)
