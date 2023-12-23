package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameDataServerMessage {
    abstract val timestamp: Long
}

@Serializable
data class GameStateSnapshotServerMessage(
    val snapshot: GameSnapshot,
    val timestamp: Long,
)

@Serializable
data class UnapprovedGameStateUpdateServerMessage(
    override val timestamp: Long,
) : GameDataServerMessage()

@Serializable
data class UserActionServerMessage(
    val action: UserAction,
    override val timestamp: Long,
) : GameDataServerMessage()

@Serializable
data class ReceiveMessageServerMessage(
    val message: UserMessage,
    override val timestamp: Long,
) : GameDataServerMessage()

@Serializable
sealed class GameDataClientMessage {
    abstract val timestamp: Long
}

@Serializable
data class GameStateUpdateClientMessage(
    val update: GameUpdate,
    val timestamp: Long,
)

@Serializable
data class HeartBeatClientMessage(
    override val timestamp: Long,
) : GameDataClientMessage()

@Serializable
data class UserActionClientMessage(
    val forUsername: Username,
    val action: UserAction,
    override val timestamp: Long,
) : GameDataClientMessage()

@Serializable
enum class UserAction { Approve, Discard }

@Serializable
data class SendMessageClientMessage(
    val message: UserMessage,
    override val timestamp: Long,
) : GameDataClientMessage()
