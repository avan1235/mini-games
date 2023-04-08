package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
internal actual fun leckerliOne(): FontFamily {
    val context = LocalContext.current
    val id = context.resources.getIdentifier("leckerlione_regular", "font", context.packageName)
    return FontFamily(Font(id, FontWeight.Normal, FontStyle.Normal))
}
