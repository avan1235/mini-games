package ml.dev.kotlin.minigames.shared.component

import androidx.compose.material3.SnackbarDuration
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.ui.util.coroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.combine as coroutinesFlowCombine
import kotlinx.coroutines.flow.map as coroutinesFlowMap
import ml.dev.kotlin.minigames.shared.ui.util.asValue as asValueUtil

interface Component {
    val appContext: MiniGamesAppComponentContext

    fun toast(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    )
}

abstract class AbstractComponent(
    final override val appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
) : ComponentContext by componentContext, Component {

    protected val scope: CoroutineScope = coroutineScope()

    override fun toast(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration,
    ) {
        scope.launch {
            appContext.snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
        }
    }

    protected fun <T, M> StateFlow<T>.map(
        coroutineScope: CoroutineScope = scope,
        mapper: (value: T) -> M,
    ): StateFlow<M> =
        coroutinesFlowMap(mapper)
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                mapper(value),
            )

    protected fun <T1, T2, R> combine(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        coroutineScope: CoroutineScope = scope,
        transform: (T1, T2) -> R,
    ): StateFlow<R> =
        coroutinesFlowCombine(flow1, flow2, transform)
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                transform(flow1.value, flow2.value)
            )

    protected fun <T1, T2, T3, R> combine(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        flow3: StateFlow<T3>,
        coroutineScope: CoroutineScope = scope,
        transform: (T1, T2, T3) -> R,
    ): StateFlow<R> =
        coroutinesFlowCombine(flow1, flow2, flow3, transform)
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                transform(flow1.value, flow2.value, flow3.value)
            )

    protected fun <T1, T2, T3, T4, R> combine(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        flow3: StateFlow<T3>,
        flow4: StateFlow<T4>,
        coroutineScope: CoroutineScope = scope,
        transform: (T1, T2, T3, T4) -> R,
    ): StateFlow<R> =
        coroutinesFlowCombine(flow1, flow2, flow3, flow4, transform)
            .stateIn(
                coroutineScope,
                SharingStarted.Eagerly,
                transform(flow1.value, flow2.value, flow3.value, flow4.value)
            )

    protected fun <T : Any> StateFlow<T>.asValue(
        lifecycle: Lifecycle = this@AbstractComponent.lifecycle,
        context: CoroutineContext = Dispatchers.Main.immediate,
    ): Value<T> = asValueUtil(lifecycle, context)
}