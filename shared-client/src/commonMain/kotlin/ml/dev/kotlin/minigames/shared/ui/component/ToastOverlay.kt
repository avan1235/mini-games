package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@Composable
fun ToastOverlay(
  message: MutableState<ToastMessage?>,
  timeMillis: Long = 2_500
) {
  val toast = message.value ?: return
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    var visible by remember(toast) { mutableStateOf(false) }
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
      Box(
        modifier = Modifier
          .padding(24.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colors.onBackground.copy(alpha = 0.9f))
          .padding(16.dp)
      ) {
        Text(
          toast.message,
          color = MaterialTheme.colors.background,
          style = Typography.caption
        )
      }
    }
    LaunchedEffect(toast) {
      visible = true
      delay(timeMillis)
      visible = false
      message.value = null
    }
  }
}

data class ToastBuffer(val state: MutableState<ToastMessage?>)

data class ToastMessage(val message: String)

fun ToastBuffer.toast(message: ToastMessage) {
  state.value = message
}

fun ToastBuffer.toast(message: String): Unit = toast(ToastMessage(message))
