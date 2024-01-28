package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

internal actual fun getUserSettings(context: MiniGamesAppComponentContext): Settings =
    SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
