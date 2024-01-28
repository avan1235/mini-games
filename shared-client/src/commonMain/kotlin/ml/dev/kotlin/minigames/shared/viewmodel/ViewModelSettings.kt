package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.Settings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

internal expect fun getUserSettings(context: MiniGamesAppComponentContext): Settings
