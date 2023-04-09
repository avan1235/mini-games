package ml.dev.kotlin.minigames.shared.model

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class UserMessage(
        val message: String,
        val author: Username,
        val timestamp: Long,
        val uuid: String = uuid4().toString()
)
