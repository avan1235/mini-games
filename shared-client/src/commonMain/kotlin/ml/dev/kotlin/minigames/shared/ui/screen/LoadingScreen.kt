package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import ml.dev.kotlin.minigames.shared.ui.component.DotsTyping
import ml.dev.kotlin.minigames.shared.ui.theme.Typography

@Composable
fun LoadingScreen(
    loadingText: String? = null,
    loadingInitState: Boolean = false,
    loadingAction: suspend CoroutineScope.(loadingState: MutableState<Boolean>) -> Unit = {},
    loadedScreen: @Composable (loadingState: MutableState<Boolean>) -> Unit,
) {
    val loadingState = remember { mutableStateOf(loadingInitState) }
    when (loadingState.value) {
        true -> LoadingScreen(text = loadingText, action = { loadingAction(loadingState) })
        false -> Box(modifier = Modifier.fillMaxSize()) { loadedScreen(loadingState) }
    }
}

@Composable
fun LoadingScreen(
    text: String? = null,
    action: suspend CoroutineScope.() -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(MIN_LOADING_ANIMATION_TIME_MILLIS)
        action()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DotsTyping()
        Spacer(modifier = Modifier.height(16.dp))
        text?.let {
            Text(
                text = text,
                style = Typography.subtitle1,
            )
        }
    }
}

private const val MIN_LOADING_ANIMATION_TIME_MILLIS = 800L
