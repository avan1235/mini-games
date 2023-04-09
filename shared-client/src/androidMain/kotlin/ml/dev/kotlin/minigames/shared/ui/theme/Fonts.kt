package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import ml.dev.kotlin.minigames.shared.client.R

internal actual suspend fun loadLeckerliOneFont(): FontFamily =
        loadFontFamily(R.font.leckerlione_regular)

private fun loadFontFamily(id: Int): FontFamily =
        FontFamily(Font(id))
