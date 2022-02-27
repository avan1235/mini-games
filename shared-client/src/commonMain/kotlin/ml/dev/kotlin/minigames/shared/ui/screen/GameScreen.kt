package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.*
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
    snapshot: Snapshot, messages: MutableStateFlow<GameClientMessage?>
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
    is UnapprovedGameStateUpdateServerMessage -> "Wait for Admin approval".also(notifyVM::addNotification).let(::toast)
    is UserActionServerMessage -> when (msg.action) {
      UserAction.Approve -> "Approved by Admin"
      UserAction.Discard -> "Discarded by Admin"
    }.also(notifyVM::addNotification).let(::toast)
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
      rightScreen = { Notifications(notifyVM) },
      leftIcon = Icons.Outlined.Forum,
      leftIconSelected = Icons.Filled.Forum,
      centerIcon = Icons.Outlined.PeopleAlt,
      centerIconSelected = Icons.Filled.PeopleAlt,
      rightIcon = Icons.Outlined.Notifications,
      rightIconSelected = Icons.Filled.Notifications,
      backPressedHandler = navigator,
      onUp = { vm.ctx.adjustPan() },
      onDown = { vm.ctx.adjustResize() },
    )
  }
}
