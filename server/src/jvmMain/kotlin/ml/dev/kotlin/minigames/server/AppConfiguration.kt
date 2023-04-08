package ml.dev.kotlin.minigames.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.websocket.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*

fun Application.installJson() = install(ContentNegotiation) { json() }

fun Application.installWebSockets() = install(WebSockets)

fun Application.installJWTAuth() = install(Authentication) {
    jwt(Jwt.CONFIG) {
        realm = Jwt.REALM
        verifier(
            JWT.require(Algorithm.HMAC256(Jwt.SECRET))
                .withAudience(Jwt.AUDIENCE)
                .withIssuer(Jwt.ISSUER)
                .build()
        )
        validate { credential ->
            val username = credential.payload.getClaim(Jwt.CLAIM).asString()
            username.takeUnless { it.isNullOrBlank() }?.let(Jwt::User)
        }
    }
}
