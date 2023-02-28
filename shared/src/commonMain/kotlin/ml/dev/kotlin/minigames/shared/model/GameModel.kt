package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import java.util.*

typealias Username = String

@Serializable
enum class UserRole {
    Admin,
    Player;

    fun defaultState(): UserState = UserState.Approved
}

@Serializable
enum class UserState { Approved, WaitingForApproval }

@Serializable
class UserData private constructor(
    val role: UserRole,
    val state: UserState,
) {
    companion object {
        fun of(role: UserRole): UserData = UserData(role, role.defaultState())

        fun player(): UserData = of(UserRole.Player)

        fun admin(): UserData = of(UserRole.Admin)
    }

    override fun equals(other: Any?): Boolean =
        true == (other as? UserData)
            ?.let { it.role == role && it.state == state }

    override fun hashCode(): Int = Objects.hash(role, state)

    fun updateState(state: UserState): UserData = when (role) {
        UserRole.Admin -> this
        UserRole.Player -> UserData(UserRole.Player, state)
    }
}

abstract class GameState {
    abstract val users: Map<Username, UserData>
    abstract val points: Map<Username, Int>

    protected abstract fun updateWith(users: Map<Username, UserData>, points: Map<Username, Int>): GameState

    abstract fun snapshot(forUser: Username): GameSnapshot

    open fun addUser(username: Username, role: UserRole): GameState = when (users[username]?.role) {
        UserRole.Admin -> this
        else -> {
            val updatedData = users[username] ?: UserData.of(role)
            val updatedUserData = username to updatedData
            val updatedPoints = points[username] ?: 0
            val updatedUserPoints = username to updatedPoints
            updateWith(users = users + updatedUserData, points = points + updatedUserPoints)
        }
    }

    open fun removeUser(username: Username): GameState =
        if (users[username]?.role == UserRole.Admin) {
            val newAdminUsername = (users - username).keys.random()
            val adminUser = newAdminUsername to UserData.admin()
            updateWith(users - username + adminUser, points = points)
        } else {
            updateWith(users - username, points = points)
        }

    open fun changeUserState(byUser: Username, forUser: Username, action: UserAction): GameState {
        if (byUser == forUser) return this
        val byUserData = users[byUser]
        if (byUserData?.role != UserRole.Admin) return this
        val forUserData = users[forUser] ?: return this
        val updatedState = when (action) {
            UserAction.Approve -> UserState.Approved
            UserAction.Discard -> UserState.WaitingForApproval
        }
        val updatedData = forUserData.updateState(state = updatedState)
        val updatedUserData = forUser to updatedData
        return updateWith(users = users + updatedUserData, points = points)
    }

    open fun update(currMillis: Long): GameState = this
}


@Serializable
sealed interface GameUpdate {
    fun update(forUser: Username, gameState: GameState, currMillis: Long): GameState
}

@Serializable
sealed interface GameSnapshot {
    val points: Map<Username, Int>
    val users: Map<Username, UserData>
}
