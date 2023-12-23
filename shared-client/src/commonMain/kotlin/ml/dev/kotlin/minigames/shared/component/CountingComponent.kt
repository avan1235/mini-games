package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface CountingComponent : Component {
    val count: Value<Int>

    fun clearNewCount()
}

abstract class AbstractCountingComponent(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    private val countPredicate: () -> Boolean,
) : AbstractComponent(appContext, componentContext), CountingComponent {

    private val _count: MutableStateFlow<Int> = MutableStateFlow(0)
    override val count: Value<Int> = _count.asValue()

    protected fun countNew() {
        val isCountValid = countPredicate()
        if (!isCountValid) return
        _count.update { it + 1 }
    }

    override fun clearNewCount() {
        _count.update { 0 }
    }
}