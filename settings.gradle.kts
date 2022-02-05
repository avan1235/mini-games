pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  plugins {
    val kotlinVersion: String by System.getProperties()
    val agpVersion: String by System.getProperties()
    val composeVersion: String by System.getProperties()
    val buildkonfigVersion: String by System.getProperties()
    val shadowVersion: String by System.getProperties()

    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.parcelize") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.android.application") version agpVersion
    id("com.android.library") version agpVersion
    id("org.jetbrains.compose") version composeVersion
    id("com.codingfeline.buildkonfig") version buildkonfigVersion
    id("com.github.johnrengelman.shadow") version shadowVersion
  }
}

rootProject.name = "mini-games"

includeBuild("build-src")

include(":shared")
include(":shared-client")
include(":android-app")
include(":desktop-app")
include(":server")
