package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.JwtToken
import ml.dev.kotlin.minigames.shared.model.UserError
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.ui.Game
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.util.Res
import kotlin.random.Random

internal class LogInViewModel(context: ViewModelContext, scope: CoroutineScope) : ViewModel(context) {

    private val client: UserClient = ctx.keeper.getOrCreate { UserClient() }

    val serverNameState: MutableState<String> = mutableStateOf("")
    val usernameState: MutableState<String> = mutableStateOf("")
    val passwordState: MutableState<String> = mutableStateOf("")

    val serverNameErrorState: MutableState<Boolean> = mutableStateOf(false)
    val usernameErrorState: MutableState<Boolean> = mutableStateOf(false)
    val passwordErrorState: MutableState<Boolean> = mutableStateOf(false)

    val gameState: MutableState<Game> = mutableStateOf(Game.values().first())

    var serverName: String by serverNameState

    var username: String by usernameState
    var password: String by passwordState
    var rememberUserLogin: Boolean by mutableStateOf(true)

    private val userLogin: UserLogin get() = UserLogin(username, password)

    private val storableServerName: String?
        get() = serverName.takeIf { shouldStoreServerName(it) }

    init {
        scope.launch {
            loadLoginScreenData()
            if (storableServerName == null) shuffleGameName()
        }
    }

    fun navigateGame(navigator: Navigator<ScreenRoute>): Unit = when (gameState.value) {
        Game.Set -> navigator.navigate(ScreenRoute.GameScreen.Set(serverName, username, password))
        Game.SnakeIO -> navigator.navigate(ScreenRoute.GameScreen.Snake(serverName, username, password))
        Game.Bird -> navigator.navigate(ScreenRoute.GameScreen.Bird(serverName, username, password))
    }

    fun shuffleGameName() {
        serverName = gameState.value.name + "-" + Random.nextInt(0, 1000)
    }

    suspend fun loginUser(): Res<UserError, JwtToken>? {
        storeLoginScreenData()
        return client.loginUser(userLogin)
    }

    @OptIn(ExperimentalSettingsApi::class)
    private suspend fun loadLoginScreenData(): Unit = getUserSettings(ctx).run {
        getStringOrNull(USERNAME_KEY)?.let { username = it }
        getStringOrNull(PASSWORD_KEY)?.let { password = it }
        getStringOrNull(SERVER_NAME_KEY)?.let { serverName = it }
        getBooleanOrNull(REMEMBER_KEY)?.let { rememberUserLogin = it }
    }

    @OptIn(ExperimentalSettingsApi::class)
    private suspend fun storeLoginScreenData(): Unit = getUserSettings(ctx).run {
        putString(USERNAME_KEY, if (rememberUserLogin) username else "")
        putString(PASSWORD_KEY, if (rememberUserLogin) password else "")
        putString(SERVER_NAME_KEY, storableServerName ?: "")
        putBoolean(REMEMBER_KEY, rememberUserLogin)
    }
}

private fun shouldStoreServerName(name: String): Boolean {
    if (name.isBlank()) return false

    return Game.values().all { !name.startsWith("${it.name}-") }
}

private const val SERVER_NAME_KEY: String = "serverName"
private const val REMEMBER_KEY: String = "remember"
private const val USERNAME_KEY: String = "username"
private const val PASSWORD_KEY: String = "password"
