package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.aakira.napier.Napier
import ml.dev.kotlin.minigames.shared.component.Game
import ml.dev.kotlin.minigames.shared.component.LogInComponent
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.ui.theme.capitals
import ml.dev.kotlin.minigames.shared.ui.theme.loadLeckerliOneFont

@Composable
internal fun LogInScreen(component: LogInComponent) {
    LoadingScreen(
        loadingText = "Logging in",
        loadingInitState = false,
        loadingAction = { loading ->
            component.loginUser(
                onError = { loading.value = false }
            )
        },
        loadedScreen = { loading ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.BottomEnd
            ) {
                ProportionKeeper {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            val fontFamily by produceLeckerliOneFont()

                            Text(
                                text = "Mini Games",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = Typography.displaySmall.copy(
                                    fontFeatureSettings = capitals,
                                    fontFamily = fontFamily ?: FontFamily.Cursive
                                ),
                            )
                            Spacer(Modifier.size(16.dp))
                            val game by component.game.subscribeAsState()
                            DropdownMenu(game, component::onGameChanged, Game.entries)
                            Spacer(Modifier.size(8.dp))
                            val serverName by component.serverName.subscribeAsState()
                            val serverNameError by component.serverNameError.subscribeAsState()
                            FormField(
                                text = "Game Server Name",
                                textInput = serverName,
                                onTextInputChange = component::onServerNameChanged,
                                errorState = serverNameError,
                                onErrorStateChange = component::onServerNameErrorChanged
                            ) {
                                IconButton(onClick = component::shuffleGameName) {
                                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "shuffle")
                                }
                            }
                            Spacer(Modifier.size(8.dp))
                            val username by component.username.subscribeAsState()
                            val usernameError by component.usernameError.subscribeAsState()
                            FormField(
                                text = "Username",
                                textInput = username,
                                onTextInputChange = component::onUsernameChanged,
                                errorState = usernameError,
                                onErrorStateChange = component::onUsernameErrorChanged
                            )
                            Spacer(Modifier.size(8.dp))
                            val password by component.password.subscribeAsState()
                            val passwordError by component.passwordError.subscribeAsState()
                            FormField(
                                text = "Password",
                                textInput = password,
                                onTextInputChange = component::onPasswordChanged,
                                errorState = passwordError,
                                onErrorStateChange = component::onPasswordErrorChanged,
                                password = true,
                                buttonType = FormFieldButtonType.Done
                            )
                            Spacer(Modifier.size(8.dp))
                            RememberCheckBox(component)
                            Spacer(Modifier.size(8.dp))
                            RegisterButton(onClick = component::navigateRegister)
                        }
                    }
                }
                CircleButton(
                    icon = Icons.Filled.ArrowForward,
                    contentDescription = "login",
                    onClick = { loading.value = component.verifyInputFields() }
                )
            }
        }
    )
}

@Composable
private fun RegisterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onClick,
            shape = Shapes.medium,
        ) {
            Text(text = "Register")
        }
    }
}

@Composable
private fun RememberCheckBox(component: LogInComponent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val rememberUserLogin by component.rememberUserLogin.subscribeAsState()
        Checkbox(
            checked = rememberUserLogin,
            onCheckedChange = component::onRememberUserLoginChanged,
        )
        Text(text = "Remember", style = Typography.titleSmall)
    }
}

@Composable
private fun produceLeckerliOneFont(): State<FontFamily?> = produceState<FontFamily?>(null) {
    value = try {
        loadLeckerliOneFont()
    } catch (e: Exception) {
        Napier.e { e.stackTraceToString() }
        null
    }
}
