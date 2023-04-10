package ml.dev.kotlin.minigames.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import ml.dev.kotlin.minigames.shared.model.JwtToken
import ml.dev.kotlin.minigames.util.envVar
import java.util.*

object Jwt {
    val AUDIENCE: String = envVar("JWT_AUDIENCE")
    val REALM: String = envVar("JWT_REALM")
    val SECRET: String = envVar("JWT_SECRET")
    val ISSUER: String = envVar("JWT_ISSUER")
    const val CLAIM = "username"
    const val CONFIG = "jwt-auth"
    private const val EXPIRE_IN_MILLIS = 60_000

    fun generateToken(username: String): JwtToken = JWT.create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withClaim(CLAIM, username)
        .withExpiresAt(Date(System.currentTimeMillis() + EXPIRE_IN_MILLIS))
        .sign(Algorithm.HMAC256(SECRET))
        .let(::JwtToken)

    data class User(val username: String) : Principal
}
