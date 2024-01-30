import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("build-src-plugin")
}

kotlin {
    jvmToolchain(17)

    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":shared-client"))
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop.application {
    mainClass = "ml.dev.kotlin.minigames.app.MainAppKt"
    version = VERSION

    nativeDistributions {
        targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
        packageName = "MiniGames"

        windows {
            menu = true
            upgradeUuid = "e60c3562-48f8-47db-91d9-ca54dfa92f35"
            iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.ico"))
        }

        linux {
            iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.png"))
        }

        macOS {
            bundleID = "ml.dev.kotlin.minigames.app"
            appStore = false
            iconFile.set(projectDir.resolve("src/jvmMain/resources/ic_launcher.icns"))
            signing {
                sign.set(false)
            }
        }
    }
}
