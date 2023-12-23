package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.model.UserData
import ml.dev.kotlin.minigames.shared.model.UserRole
import ml.dev.kotlin.minigames.shared.model.UserState
import ml.dev.kotlin.minigames.shared.ui.theme.ApproveColor
import ml.dev.kotlin.minigames.shared.ui.theme.DiscardColor
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserDataRow(
    username: String,
    userData: UserData,
    userPoints: Int,
    canEdit: Boolean,
    onApprove: () -> Unit,
    onDiscard: () -> Unit,
) {
    val dismissState = rememberDismissState(
        confirmValueChange = {
            when (it) {
                DismissValue.Default -> Unit
                DismissValue.DismissedToEnd -> onDiscard()
                DismissValue.DismissedToStart -> onApprove()
            }
            true
        },
        positionalThreshold = { totalDistance -> 0.2f * totalDistance })
    if (dismissState.currentValue != DismissValue.Default) LaunchedEffect(Unit) { dismissState.reset() }

    @Composable
    fun UserDataRaw() {
        val elevation by animateDpAsState(targetValue = if (dismissState.dismissDirection != null) 4.dp else 0.dp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(elevation),
            shape = Shapes.small,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Text(
                        text = username,
                        style = Typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth(0.75f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$userPoints point${if (userPoints == 1) "" else "s"}",
                        style = Typography.titleLarge
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = userData.roleIcon, contentDescription = "status")
                }
            }
        }
    }
    if (!canEdit) UserDataRaw()
    else SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    DismissValue.Default -> MaterialTheme.colorScheme.surface
                    DismissValue.DismissedToEnd -> DiscardColor
                    DismissValue.DismissedToStart -> ApproveColor
                }
            )
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.PersonRemove
                DismissDirection.EndToStart -> Icons.Default.PersonAddAlt1
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
        dismissContent = { UserDataRaw() }
    )
}

private val UserData.roleIcon
    get() = when {
        role == UserRole.Admin && state == UserState.Approved -> Icons.Default.LocalPolice
        role == UserRole.Player && state == UserState.Approved -> Icons.Default.Person
        else -> Icons.Default.PersonOff
    }
