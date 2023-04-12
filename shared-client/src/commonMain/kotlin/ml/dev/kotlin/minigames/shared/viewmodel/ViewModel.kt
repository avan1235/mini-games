package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic

internal abstract class ViewModel(val ctx: ViewModelContext)

internal abstract class CountingViewModel<Value>(
    ctx: ViewModelContext,
    private val countPredicate: () -> Boolean = { true },
) : ViewModel(ctx) {
    private val _newCount: AtomicInt = atomic(0)

    protected fun countNew() {
        if (!countPredicate()) return
        _newCount.addAndGet(1)
        count.value = _newCount.value
    }

    fun clearNewNotificationsCount() {
        _newCount.getAndSet(0)
        count.value = _newCount.value
    }

    val count: MutableState<Int> = mutableStateOf(_newCount.value)
}
