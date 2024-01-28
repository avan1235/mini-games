package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

internal actual fun getUserSettings(context: MiniGamesAppComponentContext): Settings =
    StorageSettings()
