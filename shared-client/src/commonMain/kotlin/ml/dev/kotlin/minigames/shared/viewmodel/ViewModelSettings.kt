package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.SuspendSettings

@OptIn(ExperimentalSettingsApi::class)
internal expect fun getUserSettings(context: ViewModelContext): SuspendSettings
