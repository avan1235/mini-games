package ml.dev.kotlin.minigames.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfadeScale
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.component.set.SetGamePlay
import ml.dev.kotlin.minigames.shared.ui.component.snake.SnakeGamePlay
import ml.dev.kotlin.minigames.shared.ui.screen.GameScreen
import ml.dev.kotlin.minigames.shared.ui.screen.LogInScreen
import ml.dev.kotlin.minigames.shared.ui.screen.RegisterScreen
import ml.dev.kotlin.minigames.shared.ui.util.rememberRouter
import ml.dev.kotlin.minigames.shared.util.Named
import ml.dev.kotlin.minigames.shared.viewmodel.*
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData


@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MiniGamesRoutes(context: ViewModelContext) {
  val navigator = rememberRouter<ScreenRoute>(ScreenRoute.LogInScreen)
  val scope = rememberCoroutineScope()

  val loginVM = remember { LogInViewModel(context, scope) }
  Children(routerState = navigator.state, animation = crossfadeScale()) { screen ->
    when (val conf = screen.configuration) {
      ScreenRoute.LogInScreen -> LogInScreen(navigator, loginVM)
      ScreenRoute.RegisterScreen -> {
        val vm = remember { RegisterViewModel(context) }
        RegisterScreen(navigator, vm)
      }
      is ScreenRoute.SetGameScreen -> {
        val accessData = GameAccessData(conf.gameName, UserLogin(conf.username, conf.password))
        val vm = remember(accessData) { SetGameViewModel(context, accessData) }
        GameScreen(vm) { snapshot, messages -> SetGamePlay(navigator, vm, snapshot, messages) }
      }
      is ScreenRoute.SnakeGameScreen -> {
        val accessData = GameAccessData(conf.gameName, UserLogin(conf.username, conf.password))
        val vm = remember(context, accessData) {
          SnakeGameViewModel(context, accessData)
        }
        GameScreen(vm) { snapshot, messages -> SnakeGamePlay(navigator, vm, snapshot, messages) }
      }
    }
  }
}

sealed interface ScreenRoute : Parcelable {
  @Parcelize
  object LogInScreen : ScreenRoute

  @Parcelize
  object RegisterScreen : ScreenRoute

  @Parcelize
  data class SetGameScreen(val gameName: String, val username: Username, val password: String) :
    ScreenRoute

  @Parcelize
  data class SnakeGameScreen(val gameName: String, val username: Username, val password: String) :
    ScreenRoute
}

enum class Game : Named {
  SnakeIO,
  Set,
}

val GAMES: List<Game> = Game.values().toList()
