package ml.dev.kotlin.minigames.shared.component

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.viewmodel.CONNECT_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.RECEIVE_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.viewmodel.SEND_ERROR_MESSAGE
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient

interface GameComponent<Snapshot : GameSnapshot> : Component, ComponentContext {
    val client: GameClient
    val gameAccessData: GameAccessData
    val username: Username get() = gameAccessData.userLogin.username

    fun closeGame()

    fun onErrorLogin()

    fun onErrorReceive(e: Exception)

    fun onErrorSend(e: Exception)

    fun points(snapshot: Snapshot): Int

    fun points(forUser: Username, snapshot: Snapshot): Int

    fun userRole(snapshot: Snapshot): UserRole

    fun canEditUser(username: Username, snapshot: Snapshot): Boolean

    fun heartBeat(): HeartBeatClientMessage

    fun approve(
        username: Username,
        clientMessages: MutableSharedFlow<GameDataClientMessage>,
    )

    fun discard(
        username: Username,
        clientMessages: MutableSharedFlow<GameDataClientMessage>,
    )
}

abstract class AbstractGameComponent<Snapshot : GameSnapshot>(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    override val gameAccessData: GameAccessData,
    private val onCloseGame: (String?) -> Unit,
) : AbstractComponent(appContext, componentContext), GameComponent<Snapshot> {

    override fun closeGame() {
        onCloseGame(null)
    }

    override fun onErrorLogin() {
        onCloseGame(CONNECT_ERROR_MESSAGE)
    }

    override fun onErrorReceive(e: Exception) {
        onCloseGame(RECEIVE_ERROR_MESSAGE)
    }

    override fun onErrorSend(e: Exception) {
        onCloseGame(SEND_ERROR_MESSAGE)
    }

    override fun points(snapshot: Snapshot): Int = points(username, snapshot)

    override fun points(forUser: Username, snapshot: Snapshot): Int = snapshot.points[forUser] ?: 0

    override fun userRole(snapshot: Snapshot): UserRole = snapshot.users[username]?.role ?: DEFAULT_USER.role

    override fun canEditUser(username: Username, snapshot: Snapshot): Boolean =
        userRole(snapshot) == UserRole.Admin && username != this.username

    override fun heartBeat(): HeartBeatClientMessage = HeartBeatClientMessage(timestamp = now())

    override fun approve(
        username: Username,
        clientMessages: MutableSharedFlow<GameDataClientMessage>,
    ) {
        toast("Approving $username")
        userAction(username, UserAction.Approve, clientMessages)
    }

    override fun discard(
        username: Username,
        clientMessages: MutableSharedFlow<GameDataClientMessage>,
    ) {
        toast("Discarding $username")
        userAction(username, UserAction.Discard, clientMessages)
    }

    private fun userAction(
        username: Username,
        action: UserAction,
        clientMessages: MutableSharedFlow<GameDataClientMessage>,
    ) {
        scope.launch {
            val message = UserActionClientMessage(username, action, timestamp = now())
            clientMessages.emit(message)
        }
    }
}

const val HEARTBEAT_DELAY_MILLIS: Long = 30_000

private val DEFAULT_USER: UserData = UserData.player()