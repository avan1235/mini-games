plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("build-src-plugin")
}

kotlin {
    jvm()
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(Dependencies.kotlinxSerializationJson)
                implementation(Dependencies.kotlinxDateTime)
            }
        }
    }
}
