package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

@Composable
internal actual fun leckerliOne(): FontFamily {
    val font = Font("font/leckerlione_regular.ttf", FontWeight.Normal, FontStyle.Normal)
    return FontFamily(font)
}
