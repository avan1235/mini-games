package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormField(
  text: String,
  textInput: MutableState<String>,
  errorState: MutableState<Boolean>,
  buttonType: FormFieldButtonType = FormFieldButtonType.Next,
  password: Boolean = false,
  inputRegex: Regex = Regex("[^\\s]*"),
  trailingIcon: @Composable (() -> Unit)? = null,
) {
  val focusManager = LocalFocusManager.current
  val showPassword = remember { mutableStateOf(false) }

  OutlinedTextField(
    value = textInput.value,
    onValueChange = {
      errorState.value = false
      textInput.value = if (inputRegex.matches(it)) it else textInput.value
    },
    isError = errorState.value,
    modifier = Modifier
      .fillMaxWidth()
      .onKeyEvent {
        if (it.key.keyCode == Key.Tab.keyCode) true.also { focusManager.moveFocus(FocusDirection.Down) } else false
      },
    label = { Text(text = text) },
    shape = MaterialTheme.shapes.medium,
    singleLine = true,
    visualTransformation = if (password && !showPassword.value) PasswordVisualTransformation() else VisualTransformation.None,
    keyboardOptions = (
      if (password) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
      else KeyboardOptions.Default
      ).copy(imeAction = buttonType.imeAction),
    keyboardActions = KeyboardActions(
      onNext = on(buttonType == FormFieldButtonType.Next) { focusManager.moveFocus(FocusDirection.Down) },
      onDone = on(buttonType == FormFieldButtonType.Done) { focusManager.clearFocus() },
    ),
    trailingIcon = trailingIcon ?: if (password) {
      { PasswordIcon(showPassword) }
    } else null,
  )
}

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
