package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.model.UserRole
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameTopBar(
    points: Int,
    role: UserRole,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            AnimatedContent(targetState = points) { targetPoints ->
                Text(
                    text = "$targetPoints point${if (targetPoints == 1) "" else "s"}",
                    style = Typography.h5
                )
            }
            Text(
                text = "Role: $role",
                style = Typography.subtitle2
            )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            ShadowIcon(
                imageVector = Icons.Default.Close,
                contentDescription = "close",
                size = 36.dp,
            )
        }
    }
}
