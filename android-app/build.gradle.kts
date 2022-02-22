plugins {
  kotlin("multiplatform")
  id("com.android.application")
  id("org.jetbrains.compose")
  id("build-src-plugin")
}

group = "ml.dev.kotlin.minigames"
version = "1.0"

android {
  compileSdk = Constants.Android.compileSdk
  defaultConfig {
    applicationId = "ml.dev.kotlin.minigames"
    minSdk = Constants.Android.minSdk
    targetSdk = Constants.Android.targetSdk
    versionCode = 2
    versionName = "1.0"
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
kotlin {
  android()
  sourceSets {
    named("androidMain") {
      dependencies {
        implementation(project(":shared"))
        implementation(project(":shared-client"))
        implementation(Dependencies.androidXActivity)
        implementation(Dependencies.androidXActivityCompose)
        implementation(Dependencies.androidGoogleMaterial)
        implementation(Dependencies.essentyInstanceKeeper)
        implementation(Dependencies.decompose)
      }
    }
  }
}

loadEnv()
