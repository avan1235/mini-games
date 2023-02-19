package ml.dev.kotlin.minigames.shared.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import ml.dev.kotlin.minigames.shared.model.UserLogin

private val USER_LOGIN_KEY: Preferences.Key<String> = stringPreferencesKey("user_login")
private val USER_PASSWORD_KEY: Preferences.Key<String> = stringPreferencesKey("user_password")
private val Context.USER_LOGIN_DATA_STORE: DataStore<Preferences> by preferencesDataStore("user_login")

internal actual suspend fun storeUserLogin(
    context: ViewModelContext,
    userLogin: UserLogin,
): Unit = with(context.androidContext) {
    USER_LOGIN_DATA_STORE.edit { preferences ->
        preferences[USER_LOGIN_KEY] = userLogin.username
        preferences[USER_PASSWORD_KEY] = userLogin.password
    }
}

internal actual suspend fun loadUserLogin(
    context: ViewModelContext,
    username: (String) -> Unit,
    password: (String) -> Unit,
) {
    context.androidContext.USER_LOGIN_DATA_STORE.data.firstOrNull()?.let { preferences ->
        preferences[USER_LOGIN_KEY]?.let(username)
        preferences[USER_PASSWORD_KEY]?.let(password)
    }
}
