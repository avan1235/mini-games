package ml.dev.kotlin.minigames.db.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

abstract class BaseUUIDTable(name: String) : UUIDTable(name) {
    val createdAt = timestamp("created_at").clientDefault(Instant::now)
    val updatedAt = timestamp("updated_at").nullable()
}

abstract class BaseUUIDEntity(id: EntityID<UUID>, table: BaseUUIDTable) : UUIDEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class BaseUUIDEntityClass<E : BaseUUIDEntity>(table: BaseUUIDTable) : UUIDEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.updatedAt = Instant.now()
                } catch (_: Exception) {
                }
            }
        }
    }
}
