package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import ml.dev.kotlin.minigames.shared.ui.GAMES
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.ui.theme.capitals
import ml.dev.kotlin.minigames.shared.ui.theme.loadLeckerliOneFont
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.ui.util.set
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.LogInViewModel
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext
import ml.dev.kotlin.minigames.shared.viewmodel.message

@Composable
internal fun LogInScreen(
        navigator: Navigator<ScreenRoute>,
        vm: LogInViewModel
): Unit = with(LocalToastContext.current) {
    LoadingScreen(
            loadingText = "Logging in",
            loadingInitState = false,
            loadingAction = { loading ->
                vm.loginUser().on(
                        ok = {
                            toast("Logged in")
                            vm.navigateGame(navigator)
                        },
                        err = {
                            toast(it.reason.message())
                            loading.value = false
                        },
                        empty = {
                            toast(CONNECT_ERROR_MESSAGE)
                            loading.value = false
                        }
                )
            },
            loadedScreen = { loading ->
                Box(
                        modifier = Modifier.fillMaxSize(),
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
                                        style = Typography.h3.copy(
                                                fontFeatureSettings = capitals,
                                                fontFamily = fontFamily ?: FontFamily.Cursive
                                        ),
                                )
                                Spacer(Modifier.size(16.dp))
                                DropdownMenu(vm.gameState, GAMES)
                                Spacer(Modifier.size(8.dp))
                                FormField("Game Server Name", vm.serverNameState, vm.serverNameErrorState) {
                                    IconButton(onClick = vm::shuffleGameName) {
                                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = "shuffle")
                                    }
                                }
                                Spacer(Modifier.size(8.dp))
                                FormField("Username", vm.usernameState, vm.usernameErrorState)
                                Spacer(Modifier.size(8.dp))
                                FormField(
                                        "Password",
                                        vm.passwordState,
                                        vm.passwordErrorState,
                                        password = true,
                                        buttonType = FormFieldButtonType.Done
                                )
                                Spacer(Modifier.size(8.dp))
                                RememberCheckBox(vm)
                                Spacer(Modifier.size(8.dp))
                                RegisterButton(onClick = { navigator.navigate(ScreenRoute.RegisterScreen) })
                            }
                        }
                    }
                    CircleButton(
                            icon = Icons.Filled.ArrowForward,
                            contentDescription = "login",
                            onClick = {
                                when {
                                    vm.serverName.isEmpty() -> true.set(vm.serverNameErrorState)
                                    vm.username.isEmpty() -> true.set(vm.usernameErrorState)
                                    vm.password.isEmpty() -> true.set(vm.passwordErrorState)
                                    else -> {
                                        false.set(vm.serverNameErrorState, vm.usernameErrorState, vm.passwordErrorState)
                                        loading.value = true
                                    }
                                }
                            }
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
private fun RememberCheckBox(vm: LogInViewModel) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
                checked = vm.rememberUserLogin,
                onCheckedChange = { vm.rememberUserLogin = !vm.rememberUserLogin }
        )
        Text(text = "Remember", style = Typography.subtitle2)
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
