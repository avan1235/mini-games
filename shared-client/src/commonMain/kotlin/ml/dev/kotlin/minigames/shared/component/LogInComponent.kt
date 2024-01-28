package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.ui.util.set
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.getUserSettings
import ml.dev.kotlin.minigames.shared.viewmodel.message
import kotlin.random.Random

interface LogInComponent : Component {
    val serverName: Value<String>
    fun onServerNameChanged(serverName: String)

    val username: Value<String>
    fun onUsernameChanged(username: String)

    val password: Value<String>
    fun onPasswordChanged(password: String)

    val serverNameError: Value<Boolean>
    fun onServerNameErrorChanged(error: Boolean)

    val usernameError: Value<Boolean>
    fun onUsernameErrorChanged(error: Boolean)

    val passwordError: Value<Boolean>
    fun onPasswordErrorChanged(error: Boolean)

    val game: Value<Game>
    fun onGameChanged(game: Game)

    val rememberUserLogin: Value<Boolean>
    fun onRememberUserLoginChanged(remember: Boolean)

    fun loginUser(onError: () -> Unit)

    fun verifyInputFields(): Boolean

    fun navigatePlayGame()

    fun navigateRegister()

    fun shuffleGameName()
}

internal class LogInComponentImpl(
    appContext: MiniGamesAppComponentContext,
    private val componentContext: ComponentContext,
    private val onNavigateRegister: () -> Unit,
    private val onNavigatePlayGame: (game: Game, serverName: String, username: String, password: String) -> Unit,
) : AbstractComponent(appContext, componentContext), LogInComponent {
    private val client: UserClient = UserClient()

    private val _serverName: MutableStateFlow<String> = MutableStateFlow("")
    override val serverName: Value<String> = _serverName.asValue()
    override fun onServerNameChanged(serverName: String) {
        _serverName.value = serverName
    }

    private val _username: MutableStateFlow<String> = MutableStateFlow("")
    override val username: Value<String> = _username.asValue()
    override fun onUsernameChanged(username: String) {
        _username.value = username
    }

    private val _password: MutableStateFlow<String> = MutableStateFlow("")
    override val password: Value<String> = _password.asValue()
    override fun onPasswordChanged(password: String) {
        _password.value = password
    }

    private val _serverNameError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val serverNameError: Value<Boolean> = _serverNameError.asValue()
    override fun onServerNameErrorChanged(error: Boolean) {
        _serverNameError.value = error
    }

    private val _usernameError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val usernameError: Value<Boolean> = _usernameError.asValue()
    override fun onUsernameErrorChanged(error: Boolean) {
        _usernameError.value = error
    }

    private val _passwordError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val passwordError: Value<Boolean> = _passwordError.asValue()
    override fun onPasswordErrorChanged(error: Boolean) {
        _passwordError.value = error
    }

    private val _game: MutableStateFlow<Game> = MutableStateFlow(Game.entries.first())
    override val game: Value<Game> = _game.asValue()
    override fun onGameChanged(game: Game) {
        _game.value = game
    }

    private val _rememberUserLogin: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val rememberUserLogin: Value<Boolean> = _rememberUserLogin.asValue()
    override fun onRememberUserLoginChanged(remember: Boolean) {
        _rememberUserLogin.value = remember
    }

    private val userLogin: UserLogin get() = UserLogin(username.value, password.value)

    private val storableServerName: String?
        get() = serverName.value.takeIf { shouldStoreServerName(it) }

    init {
        scope.launch {
            loadLoginScreenData()
            if (storableServerName == null) shuffleGameName()
        }
    }

    override fun navigateRegister() {
        onNavigateRegister()
    }

    override fun verifyInputFields(): Boolean = when {
        _serverName.value.isEmpty() -> true.set(_serverNameError).let { false }
        _username.value.isEmpty() -> true.set(_usernameError).let { false }
        _password.value.isEmpty() -> true.set(_passwordError).let { false }
        else -> false.set(_serverNameError, _usernameError, _passwordError).let { true }
    }

    override fun navigatePlayGame() {
        scope.launch(Dispatchers.Main) {
            onNavigatePlayGame(_game.value, _serverName.value, _username.value, _password.value)
        }
    }

    override fun shuffleGameName() {
        _serverName.value = _game.value.name + "-" + Random.nextInt(0, 1000)
    }

    override fun loginUser(onError: () -> Unit) {
        scope.launch {
            storeLoginScreenData()
            client.loginUser(userLogin).on(
                ok = {
                    toast("Logged in")
                    navigatePlayGame()
                },
                err = {
                    toast(it.reason.message())
                    onError()
                },
                empty = {
                    toast(CONNECT_ERROR_MESSAGE)
                    onError()
                }
            )
        }
    }

    private fun loadLoginScreenData(): Unit = getUserSettings(appContext).run {
        getStringOrNull(USERNAME_KEY)?.let { _username.value = it }
        getStringOrNull(PASSWORD_KEY)?.let { _password.value = it }
        getStringOrNull(SERVER_NAME_KEY)?.let { _serverName.value = it }
        getBooleanOrNull(REMEMBER_KEY)?.let { _rememberUserLogin.value = it }
    }

    private fun storeLoginScreenData(): Unit = getUserSettings(appContext).run {
        putString(USERNAME_KEY, if (rememberUserLogin.value) username.value else "")
        putString(PASSWORD_KEY, if (rememberUserLogin.value) password.value else "")
        putString(SERVER_NAME_KEY, storableServerName ?: "")
        putBoolean(REMEMBER_KEY, rememberUserLogin.value)
    }
}

private fun shouldStoreServerName(name: String): Boolean {
    if (name.isBlank()) return false

    return Game.entries.all { !name.startsWith("${it.name}-") }
}

private const val SERVER_NAME_KEY: String = "serverName"
private const val REMEMBER_KEY: String = "remember"
private const val USERNAME_KEY: String = "username"
private const val PASSWORD_KEY: String = "password"
