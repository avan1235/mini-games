package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

internal actual suspend fun loadLeckerliOneFont(context: ViewModelContext): FontFamily =
    loadFontFamily("leckerlione_regular", context)

private fun loadFontFamily(name: String, context: ViewModelContext): FontFamily {
    val androidContext = context.androidContext
    val id = androidContext.resources.getIdentifier(name, "font", androidContext.packageName)
    return FontFamily(Font(id))
}
