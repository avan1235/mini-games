package ml.dev.kotlin.minigames.shared.api

private const val USERS = "$API_VERSION/user"
const val USER_LOGIN_POST = "$USERS/login"
const val USER_CREATE_POST = "$USERS/create"
val USER_CONFIRM_GET: (String) -> String = { "$USERS/confirm/$it" }
