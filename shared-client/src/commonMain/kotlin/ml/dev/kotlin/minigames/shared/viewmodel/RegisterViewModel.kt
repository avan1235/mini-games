package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.essenty.instancekeeper.getOrCreate
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.model.UserError
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.util.Res

class RegisterViewModel(context: ViewModelContext) : ViewModel(context) {

    private val client: UserClient = ctx.keeper.getOrCreate { UserClient() }

    val emailState: MutableState<String> = mutableStateOf("")
    val usernameState: MutableState<String> = mutableStateOf("")
    val passwordState: MutableState<String> = mutableStateOf("")
    val confirmPasswordState: MutableState<String> = mutableStateOf("")

    val emailErrorState: MutableState<Boolean> = mutableStateOf(false)
    val usernameErrorState: MutableState<Boolean> = mutableStateOf(false)
    val passwordErrorState: MutableState<Boolean> = mutableStateOf(false)
    val confirmPasswordErrorState: MutableState<Boolean> = mutableStateOf(false)

    var email by emailState
    var username by usernameState
    var password by passwordState
    var confirmPassword by confirmPasswordState

    val userCreate: UserCreate get() = UserCreate(email, username, password)

    suspend fun createUser(): Res<UserError, Unit>? = client.createUser(userCreate)
}
