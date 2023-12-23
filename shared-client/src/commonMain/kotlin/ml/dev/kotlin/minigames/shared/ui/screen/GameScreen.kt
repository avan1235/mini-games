package ml.dev.kotlin.minigames.shared.ui.screen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import ml.dev.kotlin.minigames.shared.component.ChatComponentImpl
import ml.dev.kotlin.minigames.shared.component.GameComponent
import ml.dev.kotlin.minigames.shared.component.HEARTBEAT_DELAY_MILLIS
import ml.dev.kotlin.minigames.shared.component.NotificationsComponentImpl
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.ui.component.*
import ml.dev.kotlin.minigames.shared.ui.component.ScrollScreenSection.Companion.section
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen.LEFT
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen.RIGHT
import ml.dev.kotlin.minigames.shared.util.takeTyped
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.RECEIVE_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.SEND_ERROR_MESSAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal inline fun <reified Snapshot : GameSnapshot> GameScreen(
    component: GameComponent<Snapshot>,
    crossinline gamePlay: @Composable BoxScope.(
        snapshot: Snapshot, stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
    ) -> Unit,
) {
    val selectedScreen = remember { mutableStateOf(SelectedScreen.CENTER) }
    val swipeState = rememberSwipeableState(ScreenLocation.UP)
    val accessData = component.gameAccessData

    val chatVM = remember(accessData) {
        ChatComponentImpl(component.appContext, component, swipeState.notUp(selectedScreen, LEFT), accessData.userLogin.username)
    }
    val notifyVM = remember(accessData) {
        NotificationsComponentImpl(component.appContext, component, swipeState.notUp(selectedScreen, RIGHT))
    }
    val serverMessages = remember { MutableSharedFlow<GameDataServerMessage>(extraBufferCapacity = 256) }
    val serverStateMessages = remember { MutableStateFlow<GameStateSnapshotServerMessage?>(null) }

    val clientMessages = remember { MutableSharedFlow<GameDataClientMessage>(extraBufferCapacity = 256) }
    val clientStateMessages = remember { MutableStateFlow<GameStateUpdateClientMessage?>(null) }

    var snapshot by remember { mutableStateOf<Snapshot?>(null) }

    LaunchedEffect(Unit) {
        serverMessages.collect { msg ->
            when (msg) {
                is UnapprovedGameStateUpdateServerMessage ->
                    "Wait for Admin approval".also(notifyVM::addNotification).let(component::toast)

                is UserActionServerMessage -> when (msg.action) {
                    UserAction.Approve -> "Approved by Admin"
                    UserAction.Discard -> "Discarded by Admin"
                }.also(notifyVM::addNotification).let(component::toast)

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
        component.client.startPlayingGame(
            accessData = accessData,
            serverMessages = serverMessages,
            serverStateMessages = serverStateMessages,
            clientMessages = clientMessages,
            clientStateMessages = clientStateMessages,
            onErrorLogin = component::onErrorLogin,
            onErrorReceive = component::onErrorReceive,
            onErrorSend = component::onErrorSend,
        )
    }
    LaunchedEffect(accessData) {
        while (isActive) {
            delay(HEARTBEAT_DELAY_MILLIS)
            clientMessages.emit(component.heartBeat())
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
                onSelected = chatVM::clearNewCount,
                screen = { Chat(chatVM, clientMessages) }
            ),
            center = section(
                icon = Icons.Outlined.Leaderboard,
                iconSelected = Icons.Filled.Leaderboard,
                screen = { Players(component, state, clientMessages) }
            ),
            right = section(
                icon = Icons.Outlined.Notifications,
                iconSelected = Icons.Filled.Notifications,
                iconCount = notifyVM.count,
                onSelected = notifyVM::clearNewCount,
                screen = { Notifications(notifyVM) }
            ),
            backHandler = component.backHandler,
            onUp = { component.appContext.adjustPan() },
            onDown = { component.appContext.adjustResize() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun SwipeableState<ScreenLocation>.notUp(
    selectedScreen: MutableState<SelectedScreen>,
    screen: SelectedScreen,
): () -> Boolean =
    fun() = selectedScreen.value != screen || targetValue == ScreenLocation.UP
