package ml.dev.kotlin.minigames.app

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import ml.dev.kotlin.minigames.shared.MiniGamesApp
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

fun main(): Unit = singleWindowApplication(
    title = "Mini Games",
    state = WindowState(height = DEFAULT_SIZE.height, width = DEFAULT_SIZE.width),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
    resizable = true,
) {
    val keeper = InstanceKeeperDispatcher()
    val context = ViewModelContext(keeper)
    MiniGamesApp(context)
}

private val DEFAULT_SIZE: DpSize = DpSize(480.dp, 960.dp)
