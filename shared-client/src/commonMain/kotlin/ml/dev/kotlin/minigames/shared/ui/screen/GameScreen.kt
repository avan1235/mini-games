package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.component.Players
import ml.dev.kotlin.minigames.shared.ui.component.ScrollScreen
import ml.dev.kotlin.minigames.shared.ui.component.set.SetBoard
import ml.dev.kotlin.minigames.shared.ui.component.toast
import ml.dev.kotlin.minigames.shared.ui.util.navigate
import ml.dev.kotlin.minigames.shared.util.takeTyped
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.GameViewModel
import ml.dev.kotlin.minigames.shared.viewmodel.HEARTBEAT_DELAY_MILLIS

@Composable
inline fun <reified Snapshot : GameSnapshot> GameScreen(
  vm: GameViewModel<Snapshot>,
  crossinline gamePlay: @Composable BoxScope.(
    snapshot: Snapshot,
    messages: MutableStateFlow<GameClientMessage?>
  ) -> Unit
) {
  ToastScreen {
    val serverMessages = remember { MutableStateFlow<GameServerMessage?>(null) }
    val clientMessages = remember { MutableStateFlow<GameClientMessage?>(null) }
    var snapshot by remember { mutableStateOf<Snapshot?>(null) }

    val gameAccessData = vm.gameAccessData
    val serverMessage = serverMessages.collectAsState()
    val connectError = { toast(CONNECT_ERROR_MESSAGE) }

    LaunchedEffect(gameAccessData) {
      vm.client.startPlayingGame(
        gameAccessData,
        serverMessages,
        clientMessages,
        onErrorLogin = { connectError() },
        onErrorReceive = { connectError() },
        onErrorSend = { connectError() },
      )
    }

    LaunchedEffect(gameAccessData) {
      while (isActive) {
        delay(HEARTBEAT_DELAY_MILLIS)
        clientMessages.emit(vm.heartBeat())
      }
    }

    when (val message = serverMessage.value) {
      is GameStateSnapshotServerMessage -> snapshot = message.snapshot.takeTyped()
      is UnapprovedGameStateUpdateServerMessage -> toast("Wait for approval")
      null -> Unit
    }

    when(val state = snapshot){
      null -> LoadingScreen("Loading game")
      else -> ScrollScreen(
        up = { gamePlay(state, clientMessages) },
        down = { Players(vm, state, clientMessages) },
        icon = Icons.Default.PeopleAlt,
      )
    }
  }
}
