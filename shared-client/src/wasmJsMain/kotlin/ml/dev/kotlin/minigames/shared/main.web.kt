package ml.dev.kotlin.minigames.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentImpl

@OptIn(ExperimentalComposeUiApi::class)
fun mainWebApp() {
    val lifecycle = LifecycleRegistry()
    val component = MiniGamesAppComponentImpl(
        MiniGamesAppComponentContext(),
        DefaultComponentContext(lifecycle)
    )
    lifecycle.resume()
    CanvasBasedWindow("MiniGames", canvasElementId = "minigamesCanvas") {
        MiniGamesApp(component)
    }
}
