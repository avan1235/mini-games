import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.parcelize") apply false
  kotlin("jvm") apply false
  kotlin("plugin.serialization") apply false
  id("com.android.application") apply false
  id("com.android.library") apply false
  id("org.jetbrains.compose") apply false
  id("com.codingfeline.buildkonfig") apply false
}

subprojects {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    tasks.withType<KotlinCompile> {
      kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
  }
}

tasks.register("stage") {
  dependsOn("server:shadowJar")
}
