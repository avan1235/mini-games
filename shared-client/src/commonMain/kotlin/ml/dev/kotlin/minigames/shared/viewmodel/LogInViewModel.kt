package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.instancekeeper.getOrCreate
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

class LogInViewModel(context: ViewModelContext, scope: CoroutineScope) : ViewModel(context) {

  private val client: UserClient = ctx.keeper.getOrCreate { UserClient() }

  val serverNameState: MutableState<String> = mutableStateOf("")
  val usernameState: MutableState<String> = mutableStateOf("")
  val passwordState: MutableState<String> = mutableStateOf("")

  val usernameErrorState: MutableState<Boolean> = mutableStateOf(false)
  val passwordErrorState: MutableState<Boolean> = mutableStateOf(false)
  val serverNameErrorState: MutableState<Boolean> = mutableStateOf(false)

  val gameState: MutableState<Game> = mutableStateOf(Game.values().first())

  var serverName: String by serverNameState

  var username: String by usernameState
  var password: String by passwordState
  var rememberUserLogin: Boolean by mutableStateOf(true)

  val userLogin: UserLogin get() = UserLogin(username, password)

  init {
    shuffleGameName()
    scope.launch { loadUserLogin(context, { username = it }, { password = it }) }
  }

  fun navigateGame(navigator: Navigator<ScreenRoute>): Unit = when (gameState.value) {
    Game.Set -> navigator.navigate(ScreenRoute.SetGameScreen(serverName, username, password))
    Game.SnakeIO -> navigator.navigate(ScreenRoute.SnakeGameScreen(serverName, username, password))
  }

  fun shuffleGameName() {
    serverName = gameState.value.name + "-" + Random.nextInt(0, 1000)
  }

  suspend fun loginUser(): Res<UserError, JwtToken>? {
    val userData = if (rememberUserLogin) userLogin else UserLogin("", "")
    storeUserLogin(ctx, userData)
    return client.loginUser(userLogin)
  }
}

internal expect suspend fun storeUserLogin(
  context: ViewModelContext,
  userLogin: UserLogin,
)

internal expect suspend fun loadUserLogin(
  context: ViewModelContext,
  username: (String) -> Unit,
  password: (String) -> Unit,
)
