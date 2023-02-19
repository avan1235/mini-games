@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.server.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import ml.dev.kotlin.minigames.server.Jwt
import org.slf4j.Logger

typealias RoutesCtx = PipelineContext<Unit, ApplicationCall>

inline fun RoutesCtx.log(): Logger = this.application.environment.log

@ContextDsl
inline fun <reified R : Any> Route.authJwtPost(
    path: String,
    crossinline body: suspend RoutesCtx.(R, Jwt.User) -> Unit
): Route = authenticate(Jwt.CONFIG) {
    post(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) body(call.receive(), principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}

inline fun Route.authJwtGet(
    path: String,
    crossinline body: suspend RoutesCtx.(Jwt.User) -> Unit
): Route = authenticate(Jwt.CONFIG) {
    get(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) body(principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}

fun Route.authJwtWebSocket(
    path: String,
    handler: suspend (DefaultWebSocketServerSession, Jwt.User) -> Unit
): Route = authenticate(Jwt.CONFIG) {
    webSocket(path) {
        val principal = call.principal<Jwt.User>()
        if (principal != null) handler(this, principal)
        else call.respond(HttpStatusCode.Unauthorized)
    }
}
