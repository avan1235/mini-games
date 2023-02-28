import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    id("org.gradle.java")
    id("com.github.johnrengelman.shadow")
    id("build-src-plugin")
}

group = "ml.dev.kotlin.minigames"
version = "1.7.0"

kotlin {
    jvm()
    sourceSets {
        named("jvmMain") {
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
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "${JavaVersion.VERSION_11}"
    targetCompatibility = "${JavaVersion.VERSION_11}"
}

val shadowJarTasks = tasks.withType<ShadowJar> {
    manifest {
        attributes("Main-Class" to "ml.dev.kotlin.minigames.server.ServerKt")
    }
    archiveClassifier.set("all")
    val main by kotlin.jvm().compilations
    from(main.output)
    configurations += main.compileDependencyFiles as Configuration
    configurations += main.runtimeDependencyFiles as Configuration
}

tasks.create<JavaExec>("run") {
    dependsOn(shadowJarTasks)
    mainClass.set("-jar")
    args = listOf("${buildDir.resolve("libs").resolve("server-$version-all.jar")}")
}
