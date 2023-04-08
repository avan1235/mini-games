package ml.dev.kotlin.minigames.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ml.dev.kotlin.minigames.shared.ui.MiniGamesRoutes
import ml.dev.kotlin.minigames.shared.ui.screen.ToastScreen
import ml.dev.kotlin.minigames.shared.ui.theme.Theme
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

@Composable
internal fun MiniGamesApp(context: ViewModelContext) {
    Napier.base(DebugAntilog())

    Theme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
        ) {
            ToastScreen { MiniGamesRoutes(context) }
        }
    }
}
