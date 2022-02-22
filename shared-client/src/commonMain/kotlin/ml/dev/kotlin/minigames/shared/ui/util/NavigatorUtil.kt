package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.router
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

typealias Navigator<C> = Router<C, Any>

fun <C : Any> Navigator<C>.navigate(configuration: C, dropAll: Boolean = false) {
  navigate { if (dropAll) listOf(configuration) else it + configuration }
}

@Composable
inline fun <reified C : Parcelable> rememberRouter(
  initialRoute: C,
  key: String = "${C::class.simpleName}Router",
): Navigator<C> {
  val context = rememberComponentContext()
  return remember {
    context.router(
      initialStack = { listOf(initialRoute) },
      configurationClass = C::class,
      key = key,
      handleBackButton = true,
      childFactory = { configuration, _ -> configuration }
    )
  }
}


@Composable
fun rememberComponentContext(): ComponentContext {
  val lifecycle = rememberLifecycle()
  val stateKeeper = rememberStateKeeper()
  val backPressedHandler = LocalBackPressedHandler.current ?: BackPressedDispatcher()

  return remember { DefaultComponentContext(lifecycle, stateKeeper, null, backPressedHandler) }
}

@Composable
fun rememberLifecycle(): Lifecycle {
  val lifecycle = remember { LifecycleRegistry() }

  DisposableEffect(Unit) {
    lifecycle.resume()
    onDispose { lifecycle.destroy() }
  }
  return lifecycle
}

@Composable
fun rememberStateKeeper(): StateKeeper {
  val stateRegistry = LocalSaveableStateRegistry.current
  val dispatcher = remember { StateKeeperDispatcher(stateRegistry?.consumeRestored(KEY_STATE) as ParcelableContainer?) }


  if (stateRegistry != null) {
    DisposableEffect(Unit) {
      val entry = stateRegistry.registerProvider(KEY_STATE, dispatcher::save)
      onDispose { entry.unregister() }
    }
  }

  return dispatcher
}

val LocalBackPressedHandler: ProvidableCompositionLocal<BackPressedHandler?> = staticCompositionLocalOf { null }

private const val KEY_STATE = "MINI_GAMES_STATE"

