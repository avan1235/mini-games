package ml.dev.kotlin.minigames.server.routes

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import kotlinx.html.*
import ml.dev.kotlin.minigames.shared.api.MAIN_SITE

fun Application.webRoutes() = routing {
  get(MAIN_SITE) { respondMainSite() }
}

private suspend fun RoutesCtx.respondMainSite() {
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
