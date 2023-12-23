package ml.dev.kotlin.minigames.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent.Child
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent.Child.Game
import ml.dev.kotlin.minigames.shared.ui.component.bird.BirdGamePlay
import ml.dev.kotlin.minigames.shared.ui.component.set.SetGamePlay
import ml.dev.kotlin.minigames.shared.ui.component.snake.SnakeGamePlay
import ml.dev.kotlin.minigames.shared.ui.screen.GameScreen
import ml.dev.kotlin.minigames.shared.ui.screen.LogInScreen
import ml.dev.kotlin.minigames.shared.ui.screen.RegisterScreen
import ml.dev.kotlin.minigames.shared.ui.theme.Theme

@Composable
internal fun MiniGamesApp(component: MiniGamesAppComponent) {
    Napier.base(DebugAntilog())
    Theme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.padding(bottom = 72.dp),
                    hostState = component.snackbarHostState
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Children(
                stack = component.stack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation(slide())
            ) { child ->
                when (val instance = child.instance) {
                    is Game -> when (instance) {
                        is Game.Bird -> GameScreen(instance.component) { snapshot, stateMessages ->
                            BirdGamePlay(instance.component, snapshot, stateMessages)
                        }

                        is Game.Set -> GameScreen(instance.component) { snapshot, stateMessages ->
                            SetGamePlay(instance.component, snapshot, stateMessages)
                        }

                        is Game.Snake -> GameScreen(instance.component) { snapshot, stateMessages ->
                            SnakeGamePlay(instance.component, snapshot, stateMessages)
                        }
                    }

                    is Child.LogIn -> LogInScreen(instance.component)
                    is Child.Register -> RegisterScreen(instance.component)
                }
            }
        }
    }
}
