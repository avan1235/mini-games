import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("build-src-plugin")
}

group = "ml.dev.kotlin.minigames"
version = VERSION

android {
    compileSdk = Constants.Android.compileSdk
    defaultConfig {
        applicationId = "ml.dev.kotlin.minigames"
        minSdk = Constants.Android.minSdk
        targetSdk = Constants.Android.targetSdk
        versionCode = VERSION.replace(".", "").toIntOrNull() ?: 1
        versionName = VERSION
    }
    buildTypes {
        getByName("release") {
            signingConfig
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
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
