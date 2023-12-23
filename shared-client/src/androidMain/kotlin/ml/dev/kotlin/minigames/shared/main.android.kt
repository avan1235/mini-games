package ml.dev.kotlin.minigames.shared

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponent

fun ComponentActivity.setMainAndroidApp(
    component: MiniGamesAppComponent,
): Unit = setContent {
    LockScreenPortraitOrientation()
    MiniGamesApp(component)
}

@SuppressLint("SourceLockedOrientationActivity")
@Composable
private fun LockScreenPortraitOrientation() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose { }
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity.requestedOrientation = originalOrientation }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}