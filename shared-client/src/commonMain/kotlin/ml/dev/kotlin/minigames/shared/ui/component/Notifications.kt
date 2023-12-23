package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.component.IndexedNotification
import ml.dev.kotlin.minigames.shared.component.NotificationsComponent
import ml.dev.kotlin.minigames.shared.ui.theme.DiscardColor
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@Composable
internal fun Notifications(
    component: NotificationsComponent,
) {
    val notifications by component.notifications.subscribeAsState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        items(notifications.asReversed(), key = { it.idx }) { notification ->
            Notification(notification, onRemove = { component.removeNotification(notification) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Notification(
    notification: IndexedNotification,
    onRemove: suspend () -> Unit,
    animationDuration: Int = 300,
) {
    val visible = remember { MutableTransitionState(true) }
    val scope = rememberCoroutineScope()
    val removeAndHide = {
        scope.launch {
            visible.targetState = false
            delay(animationDuration.toLong())
            onRemove()
        }
    }
    val dismissState = rememberDismissState(confirmValueChange = {
        when (it) {
            DismissValue.Default -> Unit
            DismissValue.DismissedToEnd -> removeAndHide()
            DismissValue.DismissedToStart -> removeAndHide()
        }
        true
    })

    @Composable
    fun NotificationDataRaw() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = Shapes.small,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notification.message,
                    style = Typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val animationSpec = tween<Float>(animationDuration, easing = LinearEasing)
    AnimatedVisibility(
        visible,
        enter = scaleIn(transformOrigin = TransformOrigin(0.5f, 0f), animationSpec = animationSpec),
        exit = fadeOut(animationSpec = animationSpec)
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        DismissValue.Default -> MaterialTheme.colorScheme.surface
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
}
