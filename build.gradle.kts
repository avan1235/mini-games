import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.parcelize") apply false
    kotlin("plugin.serialization") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    id("com.codingfeline.buildkonfig") apply false
    id("com.github.johnrengelman.shadow") apply false
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
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }

    afterEvaluate {
        project.extensions.findByType<KotlinMultiplatformExtension>()?.sourceSets?.removeAll {
            it.name in setOf(
                "androidAndroidTestRelease",
            )
        }
    }
}

tasks.register("stage") {
    dependsOn("server:shadowJar")
}
