plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion by System.getProperties()
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinVersion")
}

gradlePlugin {
    plugins.register("build-src-plugin") {
        id = "build-src-plugin"
        implementationClass = "BuildSrcPlugin"
    }
}
