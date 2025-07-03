package ml.dev.kotlin.minigames.server.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import ml.dev.kotlin.minigames.db.suspendedTxn
import ml.dev.kotlin.minigames.shared.api.MAIN_SITE
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.webRoutes() = routing {
    get(MAIN_SITE) { respondMainSite() }
}

private suspend fun RoutingContext.respondMainSite() {
    suspendedTxn {
        val conn = TransactionManager.current().connection
        val statement = conn.prepareStatement("SELECT version();", false)
        statement.executeQuery()
    }
    call.respondHtml {
        head {
            title { +"Mini Games" }
        }
        body {
            h1 {
                +"Download my applications at "
                a("https://play.google.com/store/apps/developer?id=Maciej+Procyk") { +"Google Play" }
            }
        }
    }
}
