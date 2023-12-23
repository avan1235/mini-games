import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
    jvmToolchain(17)

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

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

            implementation(Dependencies.kotlinxSerialization)
            implementation(Dependencies.ktorClientCore)
            implementation(Dependencies.ktorClientWebsockets)
            implementation(Dependencies.ktorClientSerialization)
            implementation(Dependencies.ktorClientContentNegotiation)

            implementation(Dependencies.napierLogger)
            implementation(Dependencies.multiplatformSettings)
            implementation(Dependencies.multiplatformSettingsCoroutines)

            implementation(Dependencies.kotlinxAtomicFu)
        }
        androidMain {
            resources.srcDirs("src/commonMain/res")

            dependencies {
                implementation(Dependencies.androidXActivity)
                implementation(Dependencies.androidXActivityCompose)
                implementation(Dependencies.decompose)

                implementation(Dependencies.ktorClientAndroid)

                implementation(Dependencies.androidXDataStorePreferences)
                implementation(Dependencies.multiplatformSettings)
                implementation(Dependencies.multiplatformSettingsCoroutines)
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
                implementation(Dependencies.multiplatformSettingsCoroutines)
                implementation(Dependencies.kotlinxCoroutinesSwing)
            }
        }

        iosMain {
            resources.srcDirs("src/commonMain/res")

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
        listOf(
            "iosX64",
            "iosArm64",
            "iosSimulatorArm64",
        ).forEach {
            create(it) {
                buildConfigField<String>("IOS_CLIENT_API_HOST")
            }
        }
        create("jvm") {
            buildConfigField<String>("DESKTOP_CLIENT_API_HOST")
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
