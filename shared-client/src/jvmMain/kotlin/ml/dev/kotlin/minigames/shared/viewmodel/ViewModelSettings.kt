package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext
import java.util.prefs.Preferences

internal actual fun getUserSettings(context: MiniGamesAppComponentContext): Settings =
    PreferencesSettings(Preferences.userRoot())
