package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface NotificationsComponent : CountingComponent {
    val notifications: Value<List<IndexedNotification>>

    fun addNotification(message: String)
    fun removeNotification(notification: IndexedNotification)
}

class NotificationsComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    countPredicate: () -> Boolean,
) : AbstractCountingComponent(appContext, componentContext, countPredicate), NotificationsComponent {
    private val _notifications: MutableStateFlow<List<IndexedNotification>> = MutableStateFlow(emptyList())
    override val notifications: Value<List<IndexedNotification>> = _notifications.asValue()

    override fun addNotification(message: String) {
        countNew()
        _notifications.update { it + IndexedNotification(message, it.size) }
    }

    override fun removeNotification(notification: IndexedNotification) {
        _notifications.update { it - notification }
    }
}

data class IndexedNotification(val message: String, val idx: Int)
