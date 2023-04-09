package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.ui.util.set
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.RegisterViewModel
import ml.dev.kotlin.minigames.shared.viewmodel.message

@Composable
internal fun RegisterScreen(
        navigator: Navigator<ScreenRoute>,
        vm: RegisterViewModel,
): Unit = with(LocalToastContext.current) {
    LoadingScreen(
            loadingText = "Registering user",
            loadingInitState = false,
            loadingAction = { loading ->
                vm.createUser()?.on(
                        ok = {
                            toast("Verify your email and check for spam messages")
                            navigator.navigate(ScreenRoute.LogInScreen, dropAll = true)
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
                Aligned(Alignment.TopStart) {
                    BackButton(onClick = { navigator.navigate(ScreenRoute.LogInScreen, dropAll = true) })
                    Aligned(Alignment.BottomEnd) {
                        ProportionKeeper {
                            Column(
                                    modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 16.dp, end = 16.dp),
                                    verticalArrangement = Arrangement.Center,
                            ) {
                                Text(text = "Register", style = Typography.h4)
                                Spacer(Modifier.size(16.dp))
                                FormField("Email", vm.emailState, vm.emailErrorState)
                                Spacer(Modifier.size(8.dp))
                                FormField("Username", vm.usernameState, vm.usernameErrorState)
                                Spacer(Modifier.size(8.dp))
                                FormField("Password", vm.passwordState, vm.passwordErrorState, password = true)
                                Spacer(Modifier.size(8.dp))
                                FormField(
                                        "Confirm password",
                                        vm.confirmPasswordState,
                                        vm.confirmPasswordErrorState,
                                        password = true,
                                        buttonType = FormFieldButtonType.Done
                                )
                            }
                        }
                        CircleButton(
                                icon = Icons.Filled.ArrowForward,
                                contentDescription = "register",
                                onClick = {
                                    when {
                                        vm.email.isEmpty() -> true.set(vm.emailErrorState)
                                        vm.username.isEmpty() -> true.set(vm.usernameErrorState)
                                        vm.password.isEmpty() -> true.set(vm.passwordErrorState)
                                        vm.confirmPassword.isEmpty() -> true.set(vm.confirmPasswordErrorState)
                                        vm.password != vm.confirmPassword -> {
                                            true.set(vm.passwordErrorState, vm.confirmPasswordErrorState)
                                            toast("Passwords don't match")
                                        }

                                        else -> {
                                            false.set(
                                                    vm.emailErrorState,
                                                    vm.usernameErrorState,
                                                    vm.passwordErrorState,
                                                    vm.confirmPasswordErrorState
                                            )
                                            loading.value = true
                                        }
                                    }
                                }
                        )
                    }
                }
            }
    )
}
