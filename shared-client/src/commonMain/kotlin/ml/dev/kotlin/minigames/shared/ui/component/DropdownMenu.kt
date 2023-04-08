package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.util.Named

@Composable
internal fun <T : Named> DropdownMenu(
    selected: MutableState<T>,
    anyItems: Collection<T>,
) {
    val items = anyItems.toList()
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        val dropDownWidth = maxWidth
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = selected.value.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .clickable(onClick = { expanded = true })
                    .background(MaterialTheme.colors.primaryVariant)
                    .padding(16.dp),
                color = MaterialTheme.colors.onPrimary,
                style = Typography.subtitle1,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "dropdown",
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colors.onPrimary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colors.primaryVariant)
                .width(dropDownWidth)
        ) {
            items.forEach {
                DropdownMenuItem(onClick = {
                    selected.value = it
                    expanded = false
                }) {
                    Text(
                        text = it.name,
                        color = MaterialTheme.colors.onPrimary,
                        style = Typography.subtitle1,
                    )
                }
            }
        }
    }
}

