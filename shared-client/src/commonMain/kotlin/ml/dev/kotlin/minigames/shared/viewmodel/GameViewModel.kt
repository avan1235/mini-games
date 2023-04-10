package ml.dev.kotlin.minigames.shared.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.model.*
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.websocket.client.GameAccessData
import ml.dev.kotlin.minigames.shared.websocket.client.GameClient


internal abstract class GameViewModel<Snapshot : GameSnapshot>(
    context: ViewModelContext,
    val gameAccessData: GameAccessData,
) : ViewModel(context) {

    abstract val client: GameClient

    val username: Username get() = gameAccessData.userLogin.username

    fun points(snapshot: Snapshot): Int = points(username, snapshot)

    fun points(forUser: Username, snapshot: Snapshot): Int = snapshot.points[forUser] ?: 0

    fun userRole(snapshot: Snapshot): UserRole = snapshot.users[username]?.role ?: DEFAULT_USER.role

    fun canEditUser(username: Username, snapshot: Snapshot): Boolean =
        userRole(snapshot) == UserRole.Admin && username != this.username

    fun heartBeat(): HeartBeatClientMessage = HeartBeatClientMessage(timestamp = now())

    suspend fun approve(
        username: Username,
        clientMessages: MutableStateFlow<GameClientMessage?>
    ): Unit = userAction(username, UserAction.Approve, clientMessages)

    suspend fun discard(
        username: Username,
        clientMessages: MutableStateFlow<GameClientMessage?>
    ): Unit = userAction(username, UserAction.Discard, clientMessages)

    private suspend fun userAction(
        username: Username,
        action: UserAction,
        clientMessages: MutableStateFlow<GameClientMessage?>
    ) {
        val message = UserActionClientMessage(username, action, timestamp = now())
        clientMessages.emit(message)
    }
}

const val HEARTBEAT_DELAY_MILLIS: Long = 30_000

private val DEFAULT_USER: UserData = UserData.player()
