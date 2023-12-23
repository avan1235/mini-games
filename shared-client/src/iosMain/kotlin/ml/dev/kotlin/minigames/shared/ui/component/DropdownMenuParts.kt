package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = androidx.compose.material3.DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    content = content,
)

@Composable
internal actual fun DropdownMenuItem(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) = androidx.compose.material3.DropdownMenuItem(
    onClick = onClick,
    text = content,
)
