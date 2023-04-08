
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import ml.dev.kotlin.minigames.shared.MiniGamesApp
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext
import platform.UIKit.UIViewController

fun MainViewController() : UIViewController = ComposeUIViewController {
    val keeper = InstanceKeeperDispatcher()
    val context = ViewModelContext(keeper)
    MiniGamesApp(context)
}