package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.ui.text.font.FontFamily
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

internal expect suspend fun loadLeckerliOneFont(context: ViewModelContext): FontFamily
