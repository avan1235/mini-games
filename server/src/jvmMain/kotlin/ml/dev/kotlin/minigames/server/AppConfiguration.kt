package ml.dev.kotlin.minigames.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
fun Application.installCbor() = install(ContentNegotiation) { cbor() }

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
