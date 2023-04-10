package ml.dev.kotlin.minigames.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ml.dev.kotlin.minigames.server.Jwt
import ml.dev.kotlin.minigames.service.UserService
import ml.dev.kotlin.minigames.shared.api.USER_CONFIRM_GET
import ml.dev.kotlin.minigames.shared.api.USER_CREATE_POST
import ml.dev.kotlin.minigames.shared.api.USER_LOGIN_POST
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.util.on
import ml.dev.kotlin.minigames.util.RoutesCtx
import ml.dev.kotlin.minigames.util.StringValuesKey
import ml.dev.kotlin.minigames.util.get

fun Application.userRoutes() = routing {
    post(USER_LOGIN_POST) { handleUserLogin() }
    post(USER_CREATE_POST) { handleUserCreate() }
    get(USER_CONFIRM_GET("{$CONFIRM_HASH}")) { handleUserConfirm() }
}

private suspend fun RoutesCtx.handleUserLogin() {
    val userLogin = call.receive<UserLogin>()
    val user = UserService.loginUser(userLogin)
    val token = user.map { Jwt.generateToken(it.username) }
    token.on(
        ok = { call.respond(HttpStatusCode.OK, it) },
        err = { call.respond(HttpStatusCode.Unauthorized, it) },
    )
}

private suspend fun RoutesCtx.handleUserCreate() {
    val userCreate = call.receive<UserCreate>()
    val user = UserService.createUser(userCreate)
    user.on(
        ok = {
            UserService.sendConfirmationEmail(it)
            call.respond(HttpStatusCode.Created)
        },
        err = { call.respond(HttpStatusCode.BadRequest, it) },
    )
}

private suspend fun RoutesCtx.handleUserConfirm() {
    val confirmHash = call.parameters[CONFIRM_HASH] ?: return
    val confirmed = UserService.confirmUser(confirmHash)
    confirmed.on(
        ok = { call.respondText("Confirmed") },
        err = { call.respondText("Not confirmed") },
    )
}

private val CONFIRM_HASH = StringValuesKey("confirmHash")
