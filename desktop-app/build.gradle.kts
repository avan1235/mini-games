import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("build-src-plugin")
}

kotlin {
  jvm("desktop")
  sourceSets {
    named("desktopMain") {
      dependencies {
        implementation(project(":shared-client"))
        implementation(compose.desktop.common)
        implementation(compose.desktop.currentOs)
        implementation(Dependencies.essentyInstanceKeeper)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "ml.dev.kotlin.minigames.app.MainAppKt"

    nativeDistributions {
      targetFormats(TargetFormat.Msi, TargetFormat.Deb)
      packageName = "MiniGames"

      windows {
        menu = true
        upgradeUuid = "e60c3562-48f8-47db-91d9-ca54dfa92f35"
        iconFile.set(project.file("icon.ico"))
      }

      linux {
        iconFile.set(project.file("icon.png"))
      }
    }
  }
}
