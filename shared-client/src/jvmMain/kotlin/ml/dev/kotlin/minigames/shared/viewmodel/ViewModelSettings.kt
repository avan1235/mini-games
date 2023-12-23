package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext
import java.util.prefs.Preferences

@OptIn(ExperimentalSettingsApi::class)
internal actual fun getUserSettings(context: MiniGamesAppComponentContext): SuspendSettings =
    PreferencesSettings(Preferences.userRoot()).toSuspendSettings()
