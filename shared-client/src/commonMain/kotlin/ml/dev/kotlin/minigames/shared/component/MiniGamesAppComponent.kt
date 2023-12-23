package ml.dev.kotlin.minigames.shared.component

import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent.Child
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.util.Named
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData

interface MiniGamesAppComponent : Component {

    val stack: Value<ChildStack<*, Child>>

    val snackbarHostState: SnackbarHostState

    fun onBackClicked(toIndex: Int)

    sealed interface Child {
        class LogIn(val component: LogInComponent) : Child
        class Register(val component: RegisterComponent) : Child
        sealed interface Game : Child {
            val component: GameComponent<*>

            class Snake(override val component: SnakeComponent) : Game
            class Set(override val component: SetComponent) : Game
            class Bird(override val component: BirdComponent) : Game
        }
    }
}

class MiniGamesAppComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
) : AbstractComponent(appContext, componentContext), MiniGamesAppComponent {

    private val navigation: StackNavigation<Config> = StackNavigation()

    private val navigationPopAndToastMessage: (String?) -> Unit
        get() = { message -> navigation.pop { message?.let(::toast) } }

    override val stack: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.LogIn,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val snackbarHostState: SnackbarHostState = appContext.snackbarHostState

    private fun child(config: Config, childComponentContext: ComponentContext): Child = when (config) {
        is Config.LogIn -> Child.LogIn(
            LogInComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext,
                onNavigateRegister = { navigation.push(Config.Register) },
                onNavigatePlayGame = { game, serverName, username, password ->
                    navigation.push(
                        when (game) {
                            Game.Bird -> Config.Game.Bird(serverName, username, password)
                            Game.SnakeIO -> Config.Game.Snake(serverName, username, password)
                            Game.Set -> Config.Game.Set(serverName, username, password)
                        }
                    )
                },
            )
        )

        is Config.Register -> Child.Register(
            RegisterComponentImpl(
                appContext = appContext,
                componentContext = childComponentContext,
                onNavigateBack = navigationPopAndToastMessage,
            )
        )

        is Config.Game -> {
            val gameAccessData = GameAccessData(config.serverName, UserLogin(config.username, config.password))
            when (config) {
                is Config.Game.Set -> Child.Game.Set(
                    SetComponentImpl(
                        appContext = appContext,
                        componentContext = childComponentContext,
                        gameAccessData = gameAccessData,
                        onCloseGame = navigationPopAndToastMessage,
                    )
                )

                is Config.Game.Snake -> Child.Game.Snake(
                    SnakeComponentImpl(
                        appContext = appContext,
                        componentContext = childComponentContext,
                        gameAccessData = gameAccessData,
                        onCloseGame = navigationPopAndToastMessage,
                    )
                )

                is Config.Game.Bird -> Child.Game.Bird(
                    BirdComponentImpl(
                        appContext = appContext,
                        componentContext = childComponentContext,
                        gameAccessData = gameAccessData,
                        onCloseGame = navigationPopAndToastMessage,
                    )
                )
            }
        }
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object LogIn : Config

        @Serializable
        data object Register : Config

        @Serializable
        sealed interface Game : Config {
            val serverName: String
            val username: Username
            val password: String

            @Serializable
            data class Set(override val serverName: String, override val username: Username, override val password: String) : Game

            @Serializable
            data class Snake(override val serverName: String, override val username: Username, override val password: String) : Game

            @Serializable
            data class Bird(override val serverName: String, override val username: Username, override val password: String) : Game
        }
    }
}

enum class Game : Named {
    Bird,
    SnakeIO,
    Set,
}
