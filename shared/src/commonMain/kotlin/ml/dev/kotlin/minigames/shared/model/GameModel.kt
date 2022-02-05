package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable

typealias Username = String

@Serializable
enum class UserRole { Admin, Player }

@Serializable
enum class UserState { Approved, WaitingForApproval }

@Serializable
data class UserData(
  val role: UserRole,
  val state: UserState
) {
  companion object {
    fun default(): UserData = UserData(role = UserRole.Player, state = UserState.WaitingForApproval)
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
      val state = if (role == UserRole.Admin) UserState.Approved else UserState.WaitingForApproval
      val updatedData = users[username] ?: UserData(role = role, state = state)
      val updatedUserData = username to updatedData
      val updatedPoints = username to 0
      updateWith(users = users + updatedUserData, points = points + updatedPoints)
    }
  }

  open fun changeUserState(byUser: Username, forUser: Username, action: UserActionClientMessage.UserAction): GameState {
    if (byUser == forUser) return this
    val byUserData = users[byUser]
    if (byUserData?.role != UserRole.Admin) return this
    val forUserData = users[forUser] ?: return this
    val updatedState = when (action) {
      UserActionClientMessage.UserAction.Approve -> UserState.Approved
      UserActionClientMessage.UserAction.Discard -> UserState.WaitingForApproval
    }
    val updatedData = forUserData.copy(state = updatedState)
    val updatedUserData = forUser to updatedData
    return updateWith(users = users + updatedUserData, points = points)
  }

  open fun update(currMillis: Long): GameState = this
}


@Serializable
sealed class GameUpdate {
  abstract fun update(forUser: Username, gameState: GameState): GameState
}

@Serializable
sealed class GameSnapshot {
  abstract val points: Map<Username, Int>
  abstract val users: Map<Username, UserData>
}
