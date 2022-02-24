package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserMessage(val message: String, val author: Username, val timestamp: Long)
