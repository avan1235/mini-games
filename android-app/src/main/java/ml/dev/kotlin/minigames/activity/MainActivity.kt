package ml.dev.kotlin.minigames.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.essenty.backpressed.BackPressedHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import ml.dev.kotlin.minigames.shared.MiniGamesApp
import ml.dev.kotlin.minigames.shared.ui.util.LocalBackPressedHandler
import ml.dev.kotlin.minigames.shared.viewmodel.ViewModelContext

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val backPressedDispatcher = BackPressedHandler(onBackPressedDispatcher)
    setContent {
      LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
      CompositionLocalProvider(LocalBackPressedHandler provides backPressedDispatcher) {
        val keeper = InstanceKeeper(viewModelStore)
        val context = ViewModelContext(keeper, LocalContext.current, window)
        MiniGamesApp(context)
      }
    }
  }
}

@Composable
private fun LockScreenOrientation(orientation: Int) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context.findActivity() ?: return@DisposableEffect onDispose { }
    val originalOrientation = activity.requestedOrientation
    activity.requestedOrientation = orientation
    onDispose { activity.requestedOrientation = originalOrientation }
  }
}

private fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}
