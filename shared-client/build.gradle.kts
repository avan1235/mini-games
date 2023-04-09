@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig")
    kotlin("plugin.serialization")
    kotlin("plugin.parcelize")
    id("com.arkivanov.parcelize.darwin")
    id("build-src-plugin")
}

kotlin {
    android()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    cocoapods {
        homepage = "https://github.com/avan1235/mini-games"
        summary = "Mini Games shared client"
        version = VERSION
        ios.deploymentTarget = Constants.iOS.deploymentTarget
        podfile = project.file("../ios-app/Podfile")
        framework {
            isStatic = true
            baseName = "shared_client"
        }
        extraSpecAttributes["resources"] = "['src/commonMain/res/**', 'src/iosMain/res/**']"
    }

    sourceSets {
        val commonMain by named("commonMain") {
            dependencies {
                implementation(project(":shared"))

                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.ui)
                api(compose.animation)
                api(compose.animationGraphics)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(Dependencies.decompose)

                implementation(Dependencies.essentyLifecycle)
                implementation(Dependencies.essentyStateKeeper)
                implementation(Dependencies.essentyParcelable)
                implementation(Dependencies.essentyInstanceKeeper)

                implementation(Dependencies.kotlinxSerialization)
                implementation(Dependencies.ktorClientCore)
                implementation(Dependencies.ktorClientWebsockets)
                implementation(Dependencies.ktorClientSerialization)
                implementation(Dependencies.ktorClientContentNegotiation)

                implementation(Dependencies.napierLogger)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsCoroutines)
            }
        }
        named("androidMain") {
            dependsOn(commonMain)

            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(Dependencies.androidXActivity)
                implementation(Dependencies.androidXActivityCompose)
                implementation(Dependencies.androidGoogleMaterial)
                implementation(Dependencies.essentyInstanceKeeper)
                implementation(Dependencies.decompose)

                implementation(Dependencies.ktorClientAndroid)

                implementation(Dependencies.androidXDataStorePreferences)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsCoroutines)
                implementation(Dependencies.multiplatformSettingsDatastore)
            }
        }
        named("desktopMain") {
            dependsOn(commonMain)

            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)
                implementation(Dependencies.essentyInstanceKeeper)

                implementation(Dependencies.ktorClientDesktop)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsCoroutines)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)

            resources.srcDirs("src/commonMain/res")

            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(Dependencies.ktorClientDarwin)
                implementation(Dependencies.parcelizeDarwinRuntime)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsCoroutines)
            }
        }
    }
}

buildkonfig {
    packageName = "ml.dev.kotlin.minigames.shared"
    objectName = "BuildConfiguration"

    defaultConfigs {
        buildConfigField<String>("REST_CLIENT_API_SCHEME")
        buildConfigField<String>("WEBSOCKET_CLIENT_API_SCHEME")
    }

    targetConfigs {
        create("android") {
            buildConfigField<String>("ANDROID_CLIENT_API_HOST")
        }
        create("ios") {
            buildConfigField<String>("IOS_CLIENT_API_HOST")
        }
        create("desktop") {
            buildConfigField<String>("DESKTOP_CLIENT_API_HOST")
        }
    }
}

android {
    compileSdk = Constants.Android.compileSdk
    namespace = "ml.dev.kotlin.minigames.shared.client"

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

fun KotlinNativeTarget.configureBinary() = apply {
    binaries.framework {
        baseName = "shared_client"
        binaryOption("bundleId", "ml.dev.kotlin.shared.client")
    }
}

inline fun <reified T> TargetConfigDsl.buildConfigField(name: String) {
    val value = ENV[name] ?: throw IllegalStateException("$name not defined")
    when (T::class) {
        String::class -> buildConfigField(STRING, name, value)
        else -> throw IllegalStateException("Not implemented for ${T::class.java.simpleName}")
    }
}
