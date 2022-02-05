package ml.dev.kotlin.minigames.shared


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ml.dev.kotlin.minigames.shared.ui.MiniGamesRoutes
import ml.dev.kotlin.minigames.shared.ui.theme.Theme
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

@Composable
fun MiniGamesApp(context: ViewModelContext) {
  Theme {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.surface)
    ) {
      MiniGamesRoutes(context)
    }
  }
}
