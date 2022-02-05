package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class JwtToken(val value: String)
