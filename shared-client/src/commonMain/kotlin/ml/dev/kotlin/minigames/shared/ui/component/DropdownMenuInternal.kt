package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
)

@Composable
internal expect fun DropdownMenuItem(
    onClick: () -> Unit,
    content: @Composable () -> Unit
)
