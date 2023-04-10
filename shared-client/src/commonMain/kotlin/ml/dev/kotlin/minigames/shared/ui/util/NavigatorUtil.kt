package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backpressed.BackPressedDispatcher
import com.arkivanov.essenty.backpressed.BackPressedHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlin.reflect.KClass

class Navigator<C : Any>(
    private val router: Router<C, Any>,
    backPressedHandler: BackPressedHandler
) : BackPressedHandler by backPressedHandler {
    val state: Value<RouterState<C, Any>> get() = router.state

    fun navigate(configuration: C, dropAll: Boolean = false) {
        router.navigate { if (dropAll) listOf(configuration) else it + configuration }
    }
}

@Composable
internal fun <C : Parcelable> rememberRouter(
    initialRoute: C,
    configurationClass: KClass<out C>,
    key: String = "${configurationClass.simpleName}Router",
): Navigator<C> {
    val context = rememberComponentContext()
    return remember {
        context.router(
            initialStack = { listOf(initialRoute) },
            configurationClass = configurationClass,
            key = key,
            handleBackButton = true,
            childFactory = { configuration, _ -> configuration }
        )
    }.let { Navigator(it, context.backPressedHandler) }
}


@Composable
internal fun rememberComponentContext(): ComponentContext {
    val lifecycle = rememberLifecycle()
    val stateKeeper = rememberStateKeeper()
    val backPressedHandler = LocalBackPressedHandler.current ?: BackPressedDispatcher()

    return remember { DefaultComponentContext(lifecycle, stateKeeper, null, backPressedHandler) }
}

@Composable
internal fun rememberLifecycle(): Lifecycle {
    val lifecycle = remember { LifecycleRegistry() }

    DisposableEffect(Unit) {
        lifecycle.resume()
        onDispose { lifecycle.destroy() }
    }
    return lifecycle
}

@Composable
internal fun rememberStateKeeper(): StateKeeper {
    val stateRegistry = LocalSaveableStateRegistry.current
    val dispatcher =
        remember { StateKeeperDispatcher(stateRegistry?.consumeRestored(KEY_STATE) as ParcelableContainer?) }

    if (stateRegistry != null) {
        DisposableEffect(Unit) {
            val entry = stateRegistry.registerProvider(KEY_STATE, dispatcher::save)
            onDispose { entry.unregister() }
        }
    }
    return dispatcher
}

internal val LocalBackPressedHandler: ProvidableCompositionLocal<BackPressedHandler?> =
    staticCompositionLocalOf { null }

private const val KEY_STATE = "MINI_GAMES_STATE"

