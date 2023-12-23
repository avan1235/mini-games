package ml.dev.kotlin.minigames.shared.viewmodel

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.SuspendSettings
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext

@OptIn(ExperimentalSettingsApi::class)
internal expect fun getUserSettings(context: MiniGamesAppComponentContext): SuspendSettings
