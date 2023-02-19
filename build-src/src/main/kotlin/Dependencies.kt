object Dependencies {

    object Version {
        val kotlinVersion by System.getProperties()
        val composeVersion by System.getProperties()
        const val ktor = "1.6.7"
        const val exposed = "0.38.2" // Keep this version compatible with the version of exposed used by krush
        const val krush = "1.0.0"
        const val simpleMail = "1.3.3"
        const val decompose = "0.5.0"
        const val essenty = "0.2.2"
    }

    val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
    val kotlinxDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"

    val ktorServerCore = "io.ktor:ktor-server-core:${Version.ktor}"
    val ktorServerNetty = "io.ktor:ktor-server-netty:${Version.ktor}"
    val ktorServerSerialization = "io.ktor:ktor-serialization:${Version.ktor}"
    val ktorServerWebsockets = "io.ktor:ktor-websockets:${Version.ktor}"
    val ktorAuth = "io.ktor:ktor-auth:${Version.ktor}"
    val ktorAuthJwt = "io.ktor:ktor-auth-jwt:${Version.ktor}"
    val ktorHtmlBuilder = "io.ktor:ktor-html-builder:${Version.ktor}"
    val ktorClientSerialization = "io.ktor:ktor-client-serialization:${Version.ktor}"
    val ktorClientCore = "io.ktor:ktor-client-core:${Version.ktor}"
    val ktorClientAndroid = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientDesktop = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientWebsockets = "io.ktor:ktor-client-websockets:${Version.ktor}"
    val logbackClassic = "ch.qos.logback:logback-classic:1.2.6"

    val simpleMailCore = "net.axay:simplekotlinmail-core:${Version.simpleMail}"
    val simpleMailClient = "net.axay:simplekotlinmail-client:${Version.simpleMail}"

    val bCrypt = "at.favre.lib:bcrypt:0.9.0"

    val napierLogger = "io.github.aakira:napier:2.4.0"

    val composeUi = "androidx.compose.ui:ui:${Version.composeVersion}"
    val composeMaterial = "androidx.compose.material:material:${Version.composeVersion}"
    val composeIcons = "androidx.compose.material:material-icons-extended:${Version.composeVersion}"
    val androidGoogleMaterial = "com.google.android.material:material:1.4.0"
    val androidXDataStorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
    val androidXActivity = "androidx.activity:activity:1.4.0"
    val androidXActivityCompose = "androidx.activity:activity-compose:1.4.0"

    val decompose = "com.arkivanov.decompose:decompose:${Version.decompose}"
    val decomposeExtensions = "com.arkivanov.decompose:extensions-compose-jetbrains:${Version.decompose}"

    val essentyParcelable = "com.arkivanov.essenty:parcelable:${Version.essenty}"
    val essentyLifecycle = "com.arkivanov.essenty:lifecycle:${Version.essenty}"
    val essentyStateKeeper = "com.arkivanov.essenty:state-keeper:${Version.essenty}"
    val essentyInstanceKeeper = "com.arkivanov.essenty:instance-keeper:${Version.essenty}"

    val exposedCore = "org.jetbrains.exposed:exposed-core:${Version.exposed}"
    val exposedDao = "org.jetbrains.exposed:exposed-dao:${Version.exposed}"
    val exposedJdbc = "org.jetbrains.exposed:exposed-jdbc:${Version.exposed}"
    val exposedJavaTime = "org.jetbrains.exposed:exposed-java-time:${Version.exposed}"

    val postgresSqlDriver = "org.postgresql:postgresql:42.3.0"

    val krushAnnotationProcessor = "pl.touk.krush:krush-annotation-processor:${Version.krush}"
    val krushRuntime = "pl.touk.krush:krush-runtime:${Version.krush}"
    val krushRuntimePostgresql = "pl.touk.krush:krush-runtime-postgresql:${Version.krush}"
}
