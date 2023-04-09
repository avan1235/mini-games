package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(
        val username: String,
        val password: String,
)

@Serializable
data class UserCreate(
        val email: String,
        val username: String,
        val password: String,
)

@Serializable
data class UserError(
        val username: String,
        val reason: Reason
) {
    enum class Reason {
        AlreadyExists, NotExists, InvalidPassword, NotConfirmed
    }
}
