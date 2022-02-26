package ml.dev.kotlin.minigames.shared.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ml.dev.kotlin.minigames.shared.model.UserLogin
import java.io.File

private val MINI_GAMES_STORE: File = System.getProperty("user.home").let(::File).resolve(".mini-games")

internal actual suspend fun storeUserLogin(
  context: ViewModelContext,
  userLogin: UserLogin,
) {
  withContext(Dispatchers.IO) {
    val userData = Json.encodeToString(userLogin)
    MINI_GAMES_STORE.writeText(userData)
  }
}

internal actual suspend fun loadUserLogin(
  context: ViewModelContext,
  username: (String) -> Unit,
  password: (String) -> Unit,
) {
  if (!MINI_GAMES_STORE.exists()) return
  withContext(Dispatchers.IO) {
    val userData = MINI_GAMES_STORE.readText()
    Json.decodeFromString<UserLogin>(userData)
      .let { (user, pass) -> username(user); password(pass) }
  }
}
