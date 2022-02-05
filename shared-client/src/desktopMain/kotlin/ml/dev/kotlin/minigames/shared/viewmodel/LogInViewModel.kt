package ml.dev.kotlin.minigames.shared.viewmodel

import ml.dev.kotlin.minigames.shared.model.UserLogin

internal actual suspend fun storeUserLogin(
  context: ViewModelContext,
  userLogin: UserLogin,
  rememberUserLogin: Boolean
) {

}

internal actual suspend fun loadUserLogin(
  context: ViewModelContext,
  username: (String) -> Unit,
  password: (String) -> Unit,
) {

}
