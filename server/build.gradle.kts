import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("build-src-plugin")
  application
}

group = "ml.dev.kotlin.minigames"
version = "1.0"

dependencies {
  implementation(project(":shared"))

  implementation(Dependencies.ktorServerCore)
  implementation(Dependencies.ktorServerNetty)
  implementation(Dependencies.ktorServerSerialization)
  implementation(Dependencies.ktorServerWebsockets)
  implementation(Dependencies.ktorAuth)
  implementation(Dependencies.ktorAuthJwt)
  implementation(Dependencies.ktorHtmlBuilder)
  implementation(Dependencies.logbackClassic)

  implementation(Dependencies.simpleMailCore)
  implementation(Dependencies.simpleMailClient)

  implementation(Dependencies.bCrypt)

  implementation(Dependencies.exposedCore)
  implementation(Dependencies.exposedDao)
  implementation(Dependencies.exposedJdbc)
  implementation(Dependencies.exposedJavaTime)
  implementation(Dependencies.postgresSqlDriver)

  api(Dependencies.krushAnnotationProcessor)
  api(Dependencies.krushRuntime)
  api(Dependencies.krushRuntimePostgresql)
  kapt(Dependencies.krushAnnotationProcessor)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin.sourceSets.all {
  languageSettings.optIn("kotlin.RequiresOptIn")
}

tasks {
  shadowJar {
    manifest {
      attributes("Main-Class" to "server.ServerKt")
    }
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    sourceCompatibility = "${JavaVersion.VERSION_11}"
    targetCompatibility = "${JavaVersion.VERSION_11}"
    jvmTarget = "11"
  }
}

tasks.withType<JavaCompile> {
  sourceCompatibility = "${JavaVersion.VERSION_11}"
  targetCompatibility = "${JavaVersion.VERSION_11}"
}

application {
  mainClass.set("ml.dev.kotlin.minigames.server.ServerKt")
}

loadEnv()
