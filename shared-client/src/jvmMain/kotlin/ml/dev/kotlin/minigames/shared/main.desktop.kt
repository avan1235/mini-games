package ml.dev.kotlin.minigames.shared

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentImpl
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.runOnUiThread

@OptIn(ExperimentalDecomposeApi::class)
fun mainDesktopApp() {
    val lifecycle = LifecycleRegistry()
    val component = runOnUiThread {
        MiniGamesAppComponentImpl(
            MiniGamesAppComponentContext(),
            DefaultComponentContext(lifecycle)
        )
    }
    application {
        val windowState = rememberWindowState(
            height = DEFAULT_SIZE.height,
            width = DEFAULT_SIZE.width
        )

        LifecycleController(lifecycle, windowState)

        Window(
            title = "Mini Games",
            onCloseRequest = ::exitApplication,
            state = windowState,
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {
            MiniGamesApp(component)
        }
    }
}

private val DEFAULT_SIZE: DpSize = DpSize(480.dp, 960.dp)