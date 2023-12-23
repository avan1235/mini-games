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
    namespace = "ml.dev.kotlin.minigames"
    defaultConfig {
        applicationId = "ml.dev.kotlin.minigames"
        minSdk = Constants.Android.minSdk
        targetSdk = Constants.Android.targetSdk
        versionCode = VERSION_CODE
        versionName = VERSION
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":shared-client"))
            implementation(Dependencies.androidXActivity)
            implementation(Dependencies.androidXActivityCompose)
            implementation(Dependencies.decompose)
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("androidx.appcompat:appcompat-resources:1.6.1")
        }
    }
}
