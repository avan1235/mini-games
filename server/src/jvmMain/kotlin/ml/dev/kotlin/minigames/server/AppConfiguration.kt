package ml.dev.kotlin.minigames.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import ml.dev.kotlin.minigames.util.envVar

@OptIn(ExperimentalSerializationApi::class)
fun Application.installCbor() = install(ContentNegotiation) { cbor() }

fun Application.installWebSockets() = install(WebSockets)

fun Application.installAutoHeadResponse() = install(AutoHeadResponse)

fun Application.installCors() {
    val corsPort = envVar<String>("CORS_PORT")
    val corsHost = envVar<String>("CORS_HOST")
    val corsScheme = envVar<String>("CORS_SCHEME")
    install(CORS) {
        allowHost("${corsHost}:${corsPort}", schemes = listOf(corsScheme))
        allowHeader(HttpHeaders.ContentType)
    }
}

fun Application.installJWTAuth() = install(Authentication) {
    jwt(Jwt.CONFIG) {
        realm = Jwt.REALM
        authHeader { call ->
            try {
                call.request.run { parseAuthorizationHeader() ?: queryParameters[HttpHeaders.Authorization]?.let { HttpAuthHeader.Single("Bearer", it) } }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
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
