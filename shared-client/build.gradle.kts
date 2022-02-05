import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("org.jetbrains.compose")
  id("com.codingfeline.buildkonfig")
  kotlin("plugin.serialization")
  kotlin("plugin.parcelize")
  id("build-src-plugin")
}

kotlin {
  android()
  jvm("desktop")
  sourceSets {
    named("commonMain") {
      dependencies {
        implementation(project(":shared"))

        api(compose.runtime)
        api(compose.foundation)
        api(compose.material)
        api(compose.materialIconsExtended)
        api(compose.ui)
        api(compose.animation)
        api(compose.animationGraphics)

        implementation(Dependencies.decompose)
        implementation(Dependencies.decomposeExtensions)

        implementation(Dependencies.essentyLifecycle)
        implementation(Dependencies.essentyStateKeeper)
        implementation(Dependencies.essentyParcelable)
        implementation(Dependencies.essentyInstanceKeeper)

        implementation(Dependencies.kotlinxSerializationJson)
        implementation(Dependencies.ktorClientCore)
        implementation(Dependencies.ktorClientWebsockets)
        implementation(Dependencies.ktorClientSerialization)

        implementation(Dependencies.napierLogger)
      }
    }
    named("androidMain") {
      resources.srcDirs("src/commonMain/res")
      dependencies {
        implementation(Dependencies.ktorClientAndroid)

        implementation(Dependencies.androidXDataStorePreferences)
      }
    }
    named("desktopMain") {
      resources.srcDirs("src/commonMain/res")
      dependencies {
        implementation(Dependencies.ktorClientDesktop)
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
    create("desktop") {
      buildConfigField<String>("DESKTOP_CLIENT_API_HOST")
    }
  }
}

android {
  compileSdk = Constants.Android.compileSdk

  defaultConfig {
    minSdk = Constants.Android.minSdk
    targetSdk = Constants.Android.targetSdk
  }

  sourceSets {
    named("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
      res.srcDirs(
        "src/androidMain/res",
        "src/commonMain/res",
      )
    }
  }
}

inline fun <reified T> TargetConfigDsl.buildConfigField(name: String) {
  val value = ENV[name] ?: throw IllegalStateException("$name not defined")
  when (T::class) {
    String::class -> buildConfigField(STRING, name, value)
    else -> throw IllegalStateException("Not implemented for ${T::class.java.simpleName}")
  }
}
