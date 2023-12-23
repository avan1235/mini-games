package ml.dev.kotlin.minigames.shared.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

private val Context.USER_LOGIN_DATA_STORE: DataStore<Preferences> by preferencesDataStore("user_settings")

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
internal actual fun getUserSettings(context: MiniGamesAppComponentContext): SuspendSettings =
    with(context.applicationContext) {
        DataStoreSettings(USER_LOGIN_DATA_STORE)
    }
