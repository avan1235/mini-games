package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.rememberSwipeableState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.component.ScrollScreenSection.Companion.section
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen.LEFT
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen.RIGHT
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.util.takeTyped
import ml.dev.kotlin.minigames.shared.viewmodel.*
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal inline fun <reified Snapshot : GameSnapshot> GameScreen(
    accessData: GameAccessData,
    conf: ScreenRoute.GameScreen,
    navigator: Navigator<ScreenRoute>,
    vm: GameViewModel<Snapshot>,
    crossinline gamePlay: @Composable BoxScope.(
        snapshot: Snapshot, stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    ) -> Unit
): Unit = with(LocalToastContext.current) {
    val selectedScreen = remember { mutableStateOf(SelectedScreen.CENTER) }
    val swipeState = rememberSwipeableState(ScreenLocation.UP)

    val chatVM = remember(accessData) { ChatViewModel(vm.ctx, swipeState.notUp(selectedScreen, LEFT), conf.username) }
    val notifyVM = remember(accessData) { NotificationsViewModel(vm.ctx, swipeState.notUp(selectedScreen, RIGHT)) }

    val serverMessages = remember { MutableSharedFlow<GameDataServerMessage>(extraBufferCapacity = 1) }
    val serverStateMessages = remember { MutableStateFlow<GameStateSnapshotServerMessage?>(null) }

    val clientMessages = remember { MutableSharedFlow<GameDataClientMessage>(extraBufferCapacity = 1) }
    val clientStateMessages = remember { MutableStateFlow<GameStateUpdateClientMessage?>(null) }

    var snapshot by remember { mutableStateOf<Snapshot?>(null) }

    LaunchedEffect(Unit) {
        serverMessages.collect { msg ->
            when (msg) {
                is UnapprovedGameStateUpdateServerMessage ->
                    "Wait for Admin approval".also(notifyVM::addNotification).let(::toast)

                is UserActionServerMessage -> when (msg.action) {
                    UserAction.Approve -> "Approved by Admin"
                    UserAction.Discard -> "Discarded by Admin"
                }.also(notifyVM::addNotification).let(::toast)

                is ReceiveMessageServerMessage -> chatVM.addMessage(msg.message)
            }
        }
    }
    LaunchedEffect(accessData) {
        serverStateMessages.collect { msg ->
            snapshot = msg?.snapshot?.takeTyped()
        }
    }
    LaunchedEffect(accessData) {
        vm.client.startPlayingGame(
            vm.gameAccessData,
            serverMessages,
            serverStateMessages,
            clientMessages,
            clientStateMessages,
            onErrorLogin = { toast(CONNECT_ERROR_MESSAGE) },
            onErrorReceive = { toast(RECEIVE_ERROR_MESSAGE) },
            onErrorSend = { toast(SEND_ERROR_MESSAGE) },
        )
    }
    LaunchedEffect(accessData) {
        while (isActive) {
            delay(HEARTBEAT_DELAY_MILLIS)
            clientMessages.emit(vm.heartBeat())
        }
    }

    when (val state = snapshot) {
        null -> LoadingScreen("Loading game")
        else -> ScrollScreen(
            selectedScreen = selectedScreen,
            swipeState = swipeState,
            up = { gamePlay(state, clientStateMessages) },
            left = section(
                icon = Icons.Outlined.Forum,
                iconSelected = Icons.Filled.Forum,
                iconCount = chatVM.count,
                onSelected = chatVM::clearNewNotificationsCount,
                screen = { Chat(chatVM, clientMessages) }
            ),
            center = section(
                icon = Icons.Outlined.Leaderboard,
                iconSelected = Icons.Filled.Leaderboard,
                screen = { Players(vm, state, clientMessages) }
            ),
            right = section(
                icon = Icons.Outlined.Notifications,
                iconSelected = Icons.Filled.Notifications,
                iconCount = notifyVM.count,
                onSelected = notifyVM::clearNewNotificationsCount,
                screen = { Notifications(notifyVM) }
            ),
            backPressedHandler = navigator,
            onUp = { vm.ctx.adjustPan() },
            onDown = { vm.ctx.adjustResize() },
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun SwipeableState<ScreenLocation>.notUp(
    selectedScreen: MutableState<SelectedScreen>,
    screen: SelectedScreen,
): () -> Boolean =
    fun() = selectedScreen.value != screen || targetValue == ScreenLocation.UP
