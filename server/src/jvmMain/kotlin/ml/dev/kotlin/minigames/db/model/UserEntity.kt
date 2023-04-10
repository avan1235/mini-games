package ml.dev.kotlin.minigames.db.model

import ml.dev.kotlin.minigames.util.sha256
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
data class UserEntity(
    val email: String,
    val username: String,
    val passwordHash: String,
    val confirmed: Boolean,
    val confirmHash: String = UUID.randomUUID().toString().sha256(),
    val createdAt: ZonedDateTime = ZonedDateTime.now(),
    @Id val id: UUID = UUID.randomUUID(),
)
