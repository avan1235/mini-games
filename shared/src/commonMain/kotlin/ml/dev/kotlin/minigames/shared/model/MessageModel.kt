package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserMessage(
    val message: String,
    val author: Username,
    val timestamp: Long,
    val uuid: String = UUID.randomUUID().toString()
)
