@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.util

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.utils.io.*
import ml.dev.kotlin.minigames.server.Jwt
import org.slf4j.Logger

inline fun RoutingContext.log(): Logger = this.application.environment.log

@KtorDsl
inline fun <reified R : Any> Route.authJwtPost(
    path: String,
    crossinline body: suspend RoutingContext.(R, Jwt.User) -> Unit,
): Route = authenticate(Jwt.CONFIG) {
    post(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) body(call.receive(), principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}

@KtorDsl
inline fun Route.authJwtGet(
    path: String,
    crossinline body: suspend RoutingContext.(Jwt.User) -> Unit,
): Route = authenticate(Jwt.CONFIG) {
    get(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) body(principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}

@KtorDsl
fun Route.authJwtWebSocket(
    path: String,
    handler: suspend (DefaultWebSocketServerSession, Jwt.User) -> Unit,
): Route = authenticate(Jwt.CONFIG) {
    webSocket(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) handler(this, principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}

@JvmInline
value class StringValuesKey(val key: String) {
    override fun toString(): String = key
}

operator fun StringValues.get(key: StringValuesKey): String? = this[key.key]
