package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ml.dev.kotlin.minigames.shared.component.RegisterComponent
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@Composable
internal fun RegisterScreen(registerComponent: RegisterComponent) {
    LoadingScreen(
        loadingText = "Registering user",
        loadingInitState = false,
        loadingAction = { loading ->
            registerComponent.createUser(
                onError = { loading.value = false }
            )
        },
        loadedScreen = { loading ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.TopStart,
                content = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd,
                        content = {
                            ProportionKeeper {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 16.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "Register",
                                        style = Typography.headlineMedium
                                    )
                                    Spacer(Modifier.size(16.dp))
                                    val email by registerComponent.email.subscribeAsState()
                                    val emailError by registerComponent.emailError.subscribeAsState()
                                    FormField(
                                        text = "Email",
                                        textInput = email,
                                        onTextInputChange = registerComponent::onEmailChanged,
                                        errorState = emailError,
                                        onErrorStateChange = registerComponent::onEmailErrorChanged
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    val username by registerComponent.username.subscribeAsState()
                                    val usernameError by registerComponent.usernameError.subscribeAsState()
                                    FormField(
                                        text = "Username",
                                        textInput = username,
                                        onTextInputChange = registerComponent::onUsernameChanged,
                                        errorState = usernameError,
                                        onErrorStateChange = registerComponent::onUsernameErrorChanged
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    val password by registerComponent.password.subscribeAsState()
                                    val passwordError by registerComponent.passwordError.subscribeAsState()
                                    FormField(
                                        text = "Password",
                                        textInput = password,
                                        onTextInputChange = registerComponent::onPasswordChanged,
                                        errorState = passwordError,
                                        onErrorStateChange = registerComponent::onPasswordErrorChanged, password = true
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    val confirmPassword by registerComponent.confirmPassword.subscribeAsState()
                                    val confirmPasswordError by registerComponent.confirmPasswordError.subscribeAsState()
                                    FormField(
                                        text = "Confirm password",
                                        textInput = confirmPassword,
                                        onTextInputChange = registerComponent::onConfirmPasswordChanged,
                                        errorState = confirmPasswordError,
                                        onErrorStateChange = registerComponent::onConfirmPasswordErrorChanged,
                                        password = true,
                                        buttonType = FormFieldButtonType.Done
                                    )
                                }
                            }
                            CircleButton(
                                icon = Icons.Filled.ArrowForward,
                                contentDescription = "register",
                                onClick = { loading.value = registerComponent.verifyInputFields() },
                            )
                        }
                    )
                    BackButton(onClick = registerComponent::navigateBack)
                }
            )
        }
    )
}
