import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("build-src-plugin")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "minigameswasmapp"
        browser {
            commonWebpackConfig {
                outputFileName = "minigameswasmapp.js"
                devServer = devServer?.copy(port = CORS_PORT) ?: KotlinWebpackConfig.DevServer(port = CORS_PORT)
            }
        }
        binaries.executable()
        applyBinaryen()
    }

    sourceSets {
        getByName("wasmJsMain").dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material)
            @OptIn(ExperimentalComposeLibrary::class)
            api(compose.components.resources)
            implementation(project(":shared-client"))
        }
    }
}

compose.experimental {
    web.application {}
}