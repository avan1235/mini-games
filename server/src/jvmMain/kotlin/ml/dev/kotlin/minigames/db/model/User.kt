package ml.dev.kotlin.minigames.db.model

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

object UsersTable : BaseUUIDTable("users") {
    val email = text("email").uniqueIndex()
    val username = text("username").uniqueIndex()
    val passwordHash = text("password_hash").index()
    val confirmed = bool("confirmed").default(false)
    val confirmHash = text("confirm_hash")
}

class UserEntity(id: EntityID<UUID>) : BaseUUIDEntity(id, UsersTable) {
    companion object : BaseUUIDEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var username by UsersTable.username
    var passwordHash by UsersTable.passwordHash
    var confirmed by UsersTable.confirmed
    var confirmHash by UsersTable.confirmHash
}

fun ResultRow.toUserEntity() = UserEntity(this[UsersTable.id]).also {
    it.email = this[UsersTable.email]
    it.username = this[UsersTable.username]
    it.passwordHash = this[UsersTable.passwordHash]
    it.confirmed = this[UsersTable.confirmed]
    it.confirmHash = this[UsersTable.confirmHash]
}
