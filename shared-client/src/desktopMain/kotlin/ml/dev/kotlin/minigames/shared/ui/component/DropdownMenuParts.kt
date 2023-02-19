package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
): Unit = androidx.compose.material.DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    content = content
)

@Composable
internal actual fun DropdownMenuItem(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
): Unit = androidx.compose.material.DropdownMenuItem(
    onClick = onClick,
    content = content,
)
