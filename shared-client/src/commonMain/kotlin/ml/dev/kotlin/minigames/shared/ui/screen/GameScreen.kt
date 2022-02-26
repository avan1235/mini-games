package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.Chat
import ml.dev.kotlin.minigames.shared.ui.component.Players
import ml.dev.kotlin.minigames.shared.ui.component.ScrollScreen
import ml.dev.kotlin.minigames.shared.ui.component.toast
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.util.takeTyped
import ml.dev.kotlin.minigames.shared.viewmodel.*

@Composable
inline fun <reified Snapshot : GameSnapshot> GameScreen(
  navigator: Navigator<ScreenRoute>,
  vm: GameViewModel<Snapshot>,
  chatVM: ChatViewModel,
  notifyVM: NotificationsViewModel,
  crossinline gamePlay: @Composable BoxScope.(
    snapshot: Snapshot,
    messages: MutableStateFlow<GameClientMessage?>
  ) -> Unit
): Unit = with(LocalToastContext.current) {
  val serverMessages = remember { MutableStateFlow<GameServerMessage?>(null) }
  val clientMessages = remember { MutableStateFlow<GameClientMessage?>(null) }
  var snapshot by remember { mutableStateOf<Snapshot?>(null) }

  val gameAccessData = vm.gameAccessData
  val serverMessage = serverMessages.collectAsState()
  val connectError = { toast(CONNECT_ERROR_MESSAGE) }

  when (val msg = serverMessage.value) {
    is GameStateSnapshotServerMessage -> snapshot = msg.snapshot.takeTyped()
    is UnapprovedGameStateUpdateServerMessage -> toast("Wait for approval")
    is UserActionServerMessage -> when (msg.action) {
      UserAction.Approve -> "Approved by Admin"
      UserAction.Discard -> "Discarded by Admin"
    }.also { notifyVM.addNotification(it) }.let { toast(it) }
    is ReceiveMessageServerMessage -> chatVM.addMessage(msg.message)
    null -> Unit
  }

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

  when (val state = snapshot) {
    null -> LoadingScreen("Loading game")
    else -> ScrollScreen(
      up = { gamePlay(state, clientMessages) },
      leftScreen = { Chat(chatVM, clientMessages) },
      centerScreen = { Players(vm, state, clientMessages) },
      rightScreen = { Box(modifier = Modifier.fillMaxSize().background(Color.Red)) },
      leftIcon = Icons.Outlined.Forum,
      leftIconSelected = Icons.Filled.Forum,
      centerIcon = Icons.Outlined.PeopleAlt,
      centerIconSelected = Icons.Filled.PeopleAlt,
      rightIcon = Icons.Outlined.Notifications,
      rightIconSelected = Icons.Filled.Notifications,
      backPressedHandler = navigator
    )
  }
}
