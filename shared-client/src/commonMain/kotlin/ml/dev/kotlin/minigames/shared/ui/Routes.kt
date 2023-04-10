package ml.dev.kotlin.minigames.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.component.bird.BirdGamePlay
import ml.dev.kotlin.minigames.shared.ui.component.set.SetGamePlay
import ml.dev.kotlin.minigames.shared.ui.component.snake.SnakeGamePlay
import ml.dev.kotlin.minigames.shared.ui.screen.GameScreen
import ml.dev.kotlin.minigames.shared.ui.screen.LogInScreen
import ml.dev.kotlin.minigames.shared.ui.screen.RegisterScreen
import ml.dev.kotlin.minigames.shared.ui.util.Children
import ml.dev.kotlin.minigames.shared.ui.util.crossfadeScale
import ml.dev.kotlin.minigames.shared.ui.util.rememberRouter
import ml.dev.kotlin.minigames.shared.util.Named
import ml.dev.kotlin.minigames.shared.viewmodel.*
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData


@Composable
internal fun MiniGamesRoutes(context: ViewModelContext) {
    val navigator = rememberRouter(ScreenRoute.LogInScreen, ScreenRoute::class)
    val scope = rememberCoroutineScope()

    val loginVM = remember { LogInViewModel(context, scope) }
    Children(routerState = navigator.state, animation = crossfadeScale()) { screen ->
        when (val conf = screen.configuration) {
            ScreenRoute.LogInScreen -> LogInScreen(navigator, loginVM)
            ScreenRoute.RegisterScreen -> {
                val vm = remember { RegisterViewModel(context) }
                RegisterScreen(navigator, vm)
            }

            is ScreenRoute.GameScreen -> {
                val accessData = GameAccessData(conf.serverName, UserLogin(conf.username, conf.password))
                when (conf) {
                    is ScreenRoute.GameScreen.Set -> {
                        val vm = remember(accessData) { SetGameViewModel(context, accessData) }
                        GameScreen(accessData, conf, navigator, vm) { snapshot, stateMessages ->
                            SetGamePlay(navigator, vm, snapshot, stateMessages)
                        }
                    }

                    is ScreenRoute.GameScreen.Snake -> {
                        val vm = remember(context, accessData) { SnakeGameViewModel(context, accessData) }
                        GameScreen(accessData, conf, navigator, vm) { snapshot, stateMessages ->
                            SnakeGamePlay(navigator, vm, snapshot, stateMessages)
                        }
                    }

                    is ScreenRoute.GameScreen.Bird -> {
                        val vm = remember(context, accessData) { BirdGameViewModel(context, accessData) }
                        GameScreen(accessData, conf, navigator, vm) { snapshot, stateMessages ->
                            BirdGamePlay(navigator, vm, snapshot, stateMessages)
                        }
                    }
                }
            }
        }
    }
}

sealed interface ScreenRoute : Parcelable {
    @Parcelize
    object LogInScreen : ScreenRoute

    @Parcelize
    object RegisterScreen : ScreenRoute

    sealed interface GameScreen : ScreenRoute {
        val serverName: String
        val username: Username
        val password: String

        @Parcelize
        data class Set(override val serverName: String, override val username: Username, override val password: String) :
            GameScreen

        @Parcelize
        data class Snake(override val serverName: String, override val username: Username, override val password: String) :
            GameScreen

        @Parcelize
        data class Bird(override val serverName: String, override val username: Username, override val password: String) :
            GameScreen
    }
}

enum class Game : Named {
    Bird,
    SnakeIO,
    Set,
}

val GAMES: List<Game> = Game.values().toList()
