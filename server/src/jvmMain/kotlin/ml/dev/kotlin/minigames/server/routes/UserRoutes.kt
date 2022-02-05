package ml.dev.kotlin.minigames.server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ml.dev.kotlin.minigames.server.Jwt
import ml.dev.kotlin.minigames.service.UserService
import ml.dev.kotlin.minigames.shared.api.USER_CONFIRM_GET
import ml.dev.kotlin.minigames.shared.api.USER_CREATE_POST
import ml.dev.kotlin.minigames.shared.api.USER_LOGIN_POST
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.util.on

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

private const val CONFIRM_HASH = "confirmHash"
