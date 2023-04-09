package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.ui.text.font.FontFamily
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

internal actual suspend fun loadLeckerliOneFont(context: ViewModelContext): FontFamily =
    loadFontFamily("leckerlione_regular.ttf", "font/leckerlione_regular.ttf")

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadFontFamily(identity: String, path: String): FontFamily {
    val fontBytes = resource(path).readBytes()
    val font = androidx.compose.ui.text.platform.Font(identity, fontBytes)
    return FontFamily(font)
}