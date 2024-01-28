package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

internal actual fun getUserSettings(context: MiniGamesAppComponentContext): Settings =
    NSUserDefaultsSettings.Factory().create("user_settings")
