package ml.dev.kotlin.minigames.shared.rest.client

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import io.ktor.utils.io.core.*
import ml.dev.kotlin.minigames.shared.api.USER_CREATE_POST
import ml.dev.kotlin.minigames.shared.api.USER_LOGIN_POST
import ml.dev.kotlin.minigames.shared.model.JwtToken
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.model.UserError
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.util.Res

class UserClient : Closeable, InstanceKeeper.Instance {

    private val client = RestJsonApiClient()

    suspend fun loginUser(userLogin: UserLogin): Res<UserError, JwtToken>? =
        client.post(USER_LOGIN_POST) {
            body = userLogin
        }

    suspend fun createUser(userCreate: UserCreate): Res<UserError, Unit>? =
        client.post(USER_CREATE_POST) {
            body = userCreate
        }

    override fun close(): Unit = client.close()
    override fun onDestroy(): Unit = close()
}
