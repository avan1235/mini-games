package ml.dev.kotlin.minigames.service

import at.favre.lib.crypto.bcrypt.BCrypt
import ml.dev.kotlin.minigames.db.model.UserEntity
import ml.dev.kotlin.minigames.db.model.UserEntityTable
import ml.dev.kotlin.minigames.db.model.insert
import ml.dev.kotlin.minigames.db.model.toUserEntity
import ml.dev.kotlin.minigames.db.suspendedTxn
import ml.dev.kotlin.minigames.shared.api.USER_CONFIRM_GET
import ml.dev.kotlin.minigames.shared.model.UserCreate
import ml.dev.kotlin.minigames.shared.model.UserError
import ml.dev.kotlin.minigames.shared.model.UserError.Reason
import ml.dev.kotlin.minigames.shared.model.UserLogin
import ml.dev.kotlin.minigames.shared.util.Res
import ml.dev.kotlin.minigames.shared.util.err
import ml.dev.kotlin.minigames.shared.util.ok
import ml.dev.kotlin.minigames.util.envVar
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object UserService {

    private const val BCRYPT_COST = 12

    private val REQUIRE_EMAIL_VERIFY = envVar<Boolean>("REQUIRE_EMAIL_VERIFY")

    private val SCHEME_EMAIL_VERIFY = envVar<String>("SCHEME_EMAIL_VERIFY")

    private val HOST_EMAIL_VERIFY = envVar<String>("HOST_EMAIL_VERIFY")

    suspend fun loginUser(userLogin: UserLogin): Res<UserError, UserEntity> = suspendedTxn {
        UserEntityTable.select { UserEntityTable.username eq userLogin.username }.singleOrNull()?.toUserEntity()
    }?.let {
        val passwordMatch = userLogin.password matchesHash it.passwordHash
        when {
            passwordMatch && it.confirmed -> it.ok()
            passwordMatch && !it.confirmed -> UserError(it.username, Reason.NotConfirmed).err()
            else -> UserError(it.username, Reason.InvalidPassword).err()
        }
    } ?: UserError(userLogin.username, Reason.NotExists).err()

    suspend fun createUser(userCreate: UserCreate): Res<UserError, UserEntity> = suspendedTxn {
        val existingUserName = UserEntityTable.select {
            UserEntityTable.username eq userCreate.username
        }.singleOrNull()?.toUserEntity()

        val existingUserEmail = UserEntityTable.select {
            UserEntityTable.email eq userCreate.email
        }.singleOrNull()?.toUserEntity()

        when {
            existingUserName == null && existingUserEmail == null -> UserEntityTable.insert(with(userCreate) {
                UserEntity(email, username, password.bcrypt(), confirmed = !REQUIRE_EMAIL_VERIFY)
            }).ok()

            existingUserEmail != null && !existingUserEmail.confirmed ->
                UserError(userCreate.username, Reason.NotConfirmed).err()

            else -> UserError(userCreate.username, Reason.AlreadyExists).err()
        }
    }

    suspend fun confirmUser(confirmHash: String): Boolean = suspendedTxn {
        UserEntityTable.update(where = { UserEntityTable.confirmHash eq confirmHash }) { it[confirmed] = true }
        UserEntityTable.select {
            UserEntityTable.confirmHash eq confirmHash
        }.singleOrNull()?.toUserEntity()?.confirmed == true
    }

    suspend fun sendConfirmationEmail(userEntity: UserEntity) {
        if (!REQUIRE_EMAIL_VERIFY) return
        val confirmUrl = createConfirmUrl(userEntity)
        EmailService.send(
            userEntity.email,
            subject = "[Mini Games] Confirm your email address",
            text = """If you have registered to Mini Games, please confirm your email address by using <a href="$confirmUrl">this link</a>"""
        )
    }

    private fun createConfirmUrl(userEntity: UserEntity): String =
        "$SCHEME_EMAIL_VERIFY://$HOST_EMAIL_VERIFY/${USER_CONFIRM_GET(userEntity.confirmHash)}"

    private fun String.bcrypt(): String = BCrypt.withDefaults().hashToString(BCRYPT_COST, toCharArray())

    private infix fun String.matchesHash(hash: String): Boolean = BCrypt.verifyer().verify(toCharArray(), hash).verified
}
