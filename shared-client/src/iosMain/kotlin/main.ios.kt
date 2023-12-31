import androidx.compose.ui.window.ComposeUIViewController
import ml.dev.kotlin.minigames.shared.MiniGamesApp
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent
import platform.UIKit.UIViewController

@Suppress("unused")
fun MainViewController(component: MiniGamesAppComponent): UIViewController = ComposeUIViewController {
    MiniGamesApp(component)
}