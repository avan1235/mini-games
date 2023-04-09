package ml.dev.kotlin.minigames.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ml.dev.kotlin.minigames.db.model.UserEntityTable
import ml.dev.kotlin.minigames.db.txn
import ml.dev.kotlin.minigames.server.routes.gameSockets
import ml.dev.kotlin.minigames.server.routes.userRoutes
import ml.dev.kotlin.minigames.server.routes.webRoutes
import ml.dev.kotlin.minigames.util.envVar
import ml.dev.kotlin.minigames.util.eprintln
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns

fun main() {
    try {
        txn { createMissingTablesAndColumns(UserEntityTable) }

        embeddedServer(
            factory = Netty,
            host = envVar("HOST"),
            port = envVar("PORT"),
            watchPaths = emptyList(),
            module = Application::gameServiceModule
        ).start(wait = true)
    } catch (e: Exception) {
        eprintln(e)
    }
}

private fun Application.gameServiceModule() {
    installCbor()
    installWebSockets()
    installJWTAuth()

    userRoutes()
    webRoutes()
    gameSockets()
}
