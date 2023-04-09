package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings
import java.util.prefs.Preferences

@OptIn(ExperimentalSettingsApi::class)
internal actual fun getUserSettings(context: ViewModelContext): SuspendSettings =
    PreferencesSettings(Preferences.userRoot()).toSuspendSettings()
