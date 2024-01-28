import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("build-src-plugin")
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(Dependencies.kotlinxSerializationCbor)
            implementation(Dependencies.kotlinxDateTime)
            implementation(Dependencies.uuid)
        }
    }
}

android {
    compileSdk = Constants.Android.compileSdk
    namespace = "ml.dev.kotlin.minigames.shared"

    defaultConfig {
        minSdk = Constants.Android.minSdk
    }

    sourceSets {
        named("main") {
            res.srcDirs(
                "src/androidMain/res",
                "src/commonMain/res",
            )
        }
    }
}
