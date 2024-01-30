import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    cocoapods {
        homepage = "https://github.com/avan1235/mini-games"
        summary = "Mini Games shared client"
        version = VERSION
        ios.deploymentTarget = Constants.iOS.deploymentTarget
        podfile = project.file("../ios-app/Podfile")
        framework {
            isStatic = true
            baseName = "shared_client"
            export(Dependencies.decompose)
            export(Dependencies.essenty)
            export(Dependencies.stateKeeper)
            export(Dependencies.parcelizeDarwinRuntime)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.ui)
            api(compose.animation)
            api(compose.animationGraphics)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation(Dependencies.decompose)
            implementation(Dependencies.decomposeExtensions)

            implementation(Dependencies.composeUtil)

            implementation(Dependencies.kotlinxSerializationCbor)
            implementation(Dependencies.kotlinxSerializationJson)
            implementation(Dependencies.ktorClientCore)
            implementation(Dependencies.ktorClientWebsockets)
            implementation(Dependencies.ktorClientSerialization)
            implementation(Dependencies.ktorClientContentNegotiation)

            implementation(Dependencies.napierLogger)
            implementation(Dependencies.multiplatformSettings)

            implementation(Dependencies.kotlinxAtomicFu)
        }
        androidMain {
            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(Dependencies.androidXActivity)
                implementation(Dependencies.androidXActivityCompose)
                implementation(Dependencies.decompose)

                implementation("androidx.preference:preference-ktx:1.2.1")

                implementation(Dependencies.ktorClientAndroid)

                implementation(Dependencies.androidXDataStorePreferences)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsDatastore)
                implementation(Dependencies.kotlinxCoroutinesAndroid)
            }
        }
        jvmMain {
            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(compose.desktop.common)
                implementation(compose.desktop.currentOs)

                implementation(Dependencies.ktorClientDesktop)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.kotlinxCoroutinesSwing)
            }
        }
        getByName("wasmJsMain").dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)

            implementation(Dependencies.ktorClientWeb)
            implementation(Dependencies.multiplatformSettings)
        }

        iosMain {
            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(Dependencies.ktorClientDarwin)
                api(Dependencies.decompose)
                api(Dependencies.essenty)
                api(Dependencies.stateKeeper)
                api(Dependencies.parcelizeDarwinRuntime)
                implementation(Dependencies.multiplatformSettings)
            }
        }
    }
}

buildkonfig {
    packageName = "ml.dev.kotlin.minigames.shared"
    objectName = "ClientBuildConfiguration"

    defaultConfigs {
        buildConfigString("REST_CLIENT_API_SCHEME")
        buildConfigString("WEBSOCKET_CLIENT_API_SCHEME")
    }

    targetConfigs {
        create("android") {
            buildConfigString("ANDROID_CLIENT_API_HOST")
        }
        listOf(
            "iosX64",
            "iosArm64",
            "iosSimulatorArm64",
        ).forEach {
            create(it) {
                buildConfigString("IOS_CLIENT_API_HOST")
            }
        }
        create("jvm") {
            buildConfigString("DESKTOP_CLIENT_API_HOST")
        }
        create("wasmJs") {
            buildConfigString("WEB_CLIENT_API_HOST")
        }
    }
}

android {
    compileSdk = Constants.Android.compileSdk
    namespace = "ml.dev.kotlin.minigames.shared.client"

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

fun TargetConfigDsl.buildConfigString(name: String) {
    val value = ENV[name] ?: throw IllegalStateException("$name not defined")
    buildConfigField(STRING, name, value)
}
