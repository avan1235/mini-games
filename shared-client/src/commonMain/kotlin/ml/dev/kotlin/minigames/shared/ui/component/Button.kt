package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircleButton(
    icon: ImageVector? = null,
    contentDescription: String? = null,
    text: String? = null,
    color: Color = MaterialTheme.colors.primaryVariant,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        modifier = Modifier
            .padding(16.dp)
            .size(56.dp),
        elevation = 8.dp,
        color = color
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            if (text != null) {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick, modifier = Modifier.size(56.dp)) {
        Icon(Icons.Default.ArrowBack, contentDescription = "back")
    }
}
