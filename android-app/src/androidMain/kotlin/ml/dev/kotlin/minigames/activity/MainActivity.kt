package ml.dev.kotlin.minigames.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.arkivanov.decompose.defaultComponentContext
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentContext
import ml.dev.kotlin.minigames.shared.component.MiniGamesAppComponentImpl
import ml.dev.kotlin.minigames.shared.setMainAndroidApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = MiniGamesAppComponentContext(applicationContext, window)
        val component = MiniGamesAppComponentImpl(appContext, defaultComponentContext())
        setMainAndroidApp(component)
    }
}
