package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.rest.client.UserClient
import ml.dev.kotlin.minigames.shared.ui.util.set
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.message

interface RegisterComponent : Component {
    val email: Value<String>
    fun onEmailChanged(email: String)

    val username: Value<String>
    fun onUsernameChanged(username: String)

    val password: Value<String>
    fun onPasswordChanged(password: String)

    val confirmPassword: Value<String>
    fun onConfirmPasswordChanged(password: String)

    val emailError: Value<Boolean>
    fun onEmailErrorChanged(error: Boolean)

    val usernameError: Value<Boolean>
    fun onUsernameErrorChanged(error: Boolean)

    val passwordError: Value<Boolean>
    fun onPasswordErrorChanged(error: Boolean)

    val confirmPasswordError: Value<Boolean>
    fun onConfirmPasswordErrorChanged(error: Boolean)

    fun navigateBack()

    fun verifyInputFields(): Boolean

    fun createUser(onError: () -> Unit)
}

internal class RegisterComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    private val onNavigateBack: (message: String?) -> Unit,
) : AbstractComponent(appContext, componentContext), RegisterComponent {
    private val client: UserClient = UserClient()

    private val _email: MutableStateFlow<String> = MutableStateFlow("")
    override val email: Value<String> = _email.asValue()
    override fun onEmailChanged(email: String) {
        _email.value = email
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

    private val _confirmPassword: MutableStateFlow<String> = MutableStateFlow("")
    override val confirmPassword: Value<String> = _confirmPassword.asValue()
    override fun onConfirmPasswordChanged(password: String) {
        _confirmPassword.value = password
    }

    private val _emailError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val emailError: Value<Boolean> = _emailError.asValue()
    override fun onEmailErrorChanged(error: Boolean) {
        _emailError.value = error
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

    private val _confirmPasswordError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val confirmPasswordError: Value<Boolean> = _confirmPasswordError.asValue()
    override fun onConfirmPasswordErrorChanged(error: Boolean) {
        _confirmPasswordError.value = error
    }

    override fun navigateBack() {
        onNavigateBack(null)
    }

    override fun verifyInputFields(): Boolean = when {
        _email.value.isEmpty() -> true.set(_emailError).let { false }
        _username.value.isEmpty() -> true.set(_usernameError).let { false }
        _password.value.isEmpty() -> true.set(_passwordError).let { false }
        _confirmPassword.value.isEmpty() -> true.set(_confirmPasswordError).let { false }
        _password.value != _confirmPassword.value -> {
            true.set(_passwordError, _confirmPasswordError)
            toast("Passwords don't match").let { false }
        }

        else -> false.set(_emailError, _usernameError, _passwordError, _confirmPasswordError).let { true }
    }

    override fun createUser(onError: () -> Unit) {
        scope.launch {
            client.createUser(UserCreate(_email.value, _username.value, _password.value))?.on(
                ok = {
                    onNavigateBack("Verify your email and check for spam messages")
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
}