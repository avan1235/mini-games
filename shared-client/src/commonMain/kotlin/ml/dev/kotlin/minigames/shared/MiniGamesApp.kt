package ml.dev.kotlin.minigames.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ml.dev.kotlin.minigames.shared.ui.MiniGamesRoutes
import ml.dev.kotlin.minigames.shared.ui.screen.ToastScreen
import ml.dev.kotlin.minigames.shared.ui.theme.Theme
import ml.dev.kotlin.minigames.shared.util.initClientLogger
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

@Composable
fun MiniGamesApp(context: ViewModelContext) {
    initClientLogger()

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
