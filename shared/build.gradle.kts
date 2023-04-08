plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("build-src-plugin")
}

kotlin {
    android()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Dependencies.kotlinxSerializationJson)
                implementation(Dependencies.kotlinxDateTime)
                implementation(Dependencies.uuid)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdk = Constants.Android.compileSdk
    namespace = "ml.dev.kotlin.minigames.shared"

    defaultConfig {
        minSdk = Constants.Android.minSdk
        targetSdk = Constants.Android.targetSdk
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
