package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.theme.DiscardColor
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
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
    items(vm.notifications.reversed()) {
      Notification(it.message, onRemove = { vm.removeNotification(it) })
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Notification(
  message: String,
  onRemove: () -> Unit,
) {
  val dismissState = rememberDismissState(confirmStateChange = {
    when (it) {
      DismissValue.Default -> Unit
      DismissValue.DismissedToEnd -> onRemove()
      DismissValue.DismissedToStart -> onRemove()
    }
    true
  })

  @Composable
  fun NotificationDataRaw() {
    val elevation by animateDpAsState(targetValue = if (dismissState.dismissDirection != null) 4.dp else 0.dp)
    Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = elevation,
      shape = Shapes.small,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colors.surface)
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = message,
          style = Typography.body1,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }

  SwipeToDismiss(
    state = dismissState,
    dismissThresholds = { FractionalThreshold(0.2f) },
    background = {
      val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
      val color by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
          DismissValue.Default -> MaterialTheme.colors.surface
          DismissValue.DismissedToEnd -> DiscardColor
          DismissValue.DismissedToStart -> DiscardColor
        }
      )
      val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Delete
        DismissDirection.EndToStart -> Icons.Default.Delete
      }
      val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue == DismissValue.Default) 0.8f else 1f
      )
      val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
      }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(color)
          .padding(start = 12.dp, end = 12.dp),
        contentAlignment = alignment
      ) {
        Icon(icon, contentDescription = "icon", modifier = Modifier.scale(scale))
      }
    },
    dismissContent = { NotificationDataRaw() }
  )
}
