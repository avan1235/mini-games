package ml.dev.kotlin.minigames.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.arkivanov.essenty.backpressed.BackPressedHandler
import ml.dev.kotlin.minigames.shared.setMainAndroidApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backPressedHandler = BackPressedHandler(onBackPressedDispatcher)
        setMainAndroidApp(backPressedHandler)
    }
}
