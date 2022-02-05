package ml.dev.kotlin.minigames.shared.viewmodel

import ml.dev.kotlin.minigames.shared.model.UserError

fun UserError.Reason.message(): String = when (this) {
  UserError.Reason.AlreadyExists -> "User already exists"
  UserError.Reason.NotExists -> "User not exists"
  UserError.Reason.InvalidPassword -> "Invalid password"
  UserError.Reason.NotConfirmed -> "Confirm email and then login"
}

const val CONNECT_ERROR_MESSAGE: String = "Error connecting to server"
