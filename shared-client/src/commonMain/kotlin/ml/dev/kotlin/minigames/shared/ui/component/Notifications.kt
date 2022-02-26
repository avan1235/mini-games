package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ml.dev.kotlin.minigames.shared.viewmodel.NotificationsViewModel

@Composable
fun Notifications(
  vm: NotificationsViewModel
) {
  val state = rememberLazyListState()
  LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    state = state,
    verticalArrangement = Arrangement.Top
  ) {
    items(vm.notifications.reversed()) { notification ->
      Notification(notification)
    }
  }
}

@Composable
private fun Notification(message: String) {

}
