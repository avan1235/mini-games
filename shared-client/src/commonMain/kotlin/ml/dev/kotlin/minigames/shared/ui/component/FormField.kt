package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
internal fun FormField(
    text: String,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    errorState: Boolean,
    onErrorStateChange: (Boolean) -> Unit,
    buttonType: FormFieldButtonType = FormFieldButtonType.Next,
    password: Boolean = false,
    inputRegex: Regex = Regex("[^\\s]*"),
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val showPassword = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textInput,
        onValueChange = {
            onErrorStateChange(false)
            onTextInputChange(if (inputRegex.matches(it)) it else textInput)
        },
        isError = errorState,
        modifier = Modifier
            .fillMaxWidth()
            .onKeyEvent { handleTabKey(it, focusManager) },
        label = { Text(text = text) },
        shape = MaterialTheme.shapes.medium,
        singleLine = true,
        visualTransformation = if (password && !showPassword.value) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions.Default.run {
            if (password) copy(keyboardType = KeyboardType.Password) else this
        }.copy(imeAction = buttonType.imeAction),
        keyboardActions = KeyboardActions(
            onNext = on(buttonType == FormFieldButtonType.Next) { focusManager.moveFocus(FocusDirection.Down) },
            onDone = on(buttonType == FormFieldButtonType.Done) { focusManager.clearFocus() },
        ),
        trailingIcon = trailingIcon ?: if (password) {
            { PasswordIcon(showPassword) }
        } else null,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleTabKey(event: KeyEvent, focusManager: FocusManager): Boolean =
    if (event.key.keyCode == Key.Tab.keyCode && moveDownOnTab()) {
        focusManager.moveFocus(FocusDirection.Down)
    } else false

enum class FormFieldButtonType(val imeAction: ImeAction) { Next(ImeAction.Next), Done(ImeAction.Done) }

private fun <A> on(
    condition: Boolean,
    action: (A) -> Unit,
): (A) -> Unit = if (condition) action else fun(_: A) {}

@Composable
private fun PasswordIcon(showPassword: MutableState<Boolean>) {
    IconButton(onClick = { showPassword.value = !showPassword.value }) {
        Icon(
            imageVector = if (showPassword.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = "password"
        )
    }
}
