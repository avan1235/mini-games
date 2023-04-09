package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings

@OptIn(ExperimentalSettingsApi::class)
internal actual fun getUserSettings(context: ViewModelContext): SuspendSettings =
        NSUserDefaultsSettings.Factory().create("user_settings").toSuspendSettings()
