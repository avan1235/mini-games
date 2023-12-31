object Dependencies {

    object Version {
        val kotlinVersion by System.getProperties()
        val composeVersion by System.getProperties()
        val parcelizeDarwinVersion by System.getProperties()
        const val ktor = "2.3.7"
        const val kotlinxSerialization = "1.6.2"
        const val exposed = "0.45.0"
        const val simpleMail = "1.3.3"
        const val decompose = "3.0.0-alpha04"
        const val essenty = "2.0.0-alpha02"
        const val multiplatformSettings = "1.1.1"
        const val coroutines = "1.7.3"
    }

    val kotlinxSerializationCbor = "org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Version.kotlinxSerialization}"
    val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.kotlinxSerialization}"
    val kotlinxAtomicFu = "org.jetbrains.kotlinx:atomicfu:0.20.2"
    val kotlinxDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"
    val uuid = "com.benasher44:uuid:0.7.0"

    val ktorServerCore = "io.ktor:ktor-server-core:${Version.ktor}"
    val ktorServerNetty = "io.ktor:ktor-server-netty:${Version.ktor}"
    val ktorServerSerialization = "io.ktor:ktor-serialization-kotlinx-cbor:${Version.ktor}"
    val ktorServerWebsockets = "io.ktor:ktor-server-websockets:${Version.ktor}"
    val ktorServerContentNegotiation = "io.ktor:ktor-server-content-negotiation:${Version.ktor}"
    val ktorServerAuth = "io.ktor:ktor-server-auth:${Version.ktor}"
    val ktorServerAuthJwt = "io.ktor:ktor-server-auth-jwt:${Version.ktor}"
    val ktorServerHtmlBuilder = "io.ktor:ktor-server-html-builder:${Version.ktor}"

    val ktorClientSerialization = "io.ktor:ktor-serialization-kotlinx-cbor:${Version.ktor}"
    val ktorClientContentNegotiation = "io.ktor:ktor-client-content-negotiation:${Version.ktor}"
    val ktorClientCore = "io.ktor:ktor-client-core:${Version.ktor}"
    val ktorClientAndroid = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientDarwin = "io.ktor:ktor-client-darwin:${Version.ktor}"
    val ktorClientDesktop = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientWebsockets = "io.ktor:ktor-client-websockets:${Version.ktor}"

    val logbackClassic = "ch.qos.logback:logback-classic:1.4.6"

    val simpleMailCore = "net.axay:simplekotlinmail-core:${Version.simpleMail}"
    val simpleMailClient = "net.axay:simplekotlinmail-client:${Version.simpleMail}"

    val bCrypt = "at.favre.lib:bcrypt:0.9.0"

    val napierLogger = "io.github.aakira:napier:2.6.1"

    val multiplatformSettings = "com.russhwolf:multiplatform-settings:${Version.multiplatformSettings}"
    val multiplatformSettingsCoroutines = "com.russhwolf:multiplatform-settings-coroutines:${Version.multiplatformSettings}"
    val multiplatformSettingsDatastore = "com.russhwolf:multiplatform-settings-datastore:${Version.multiplatformSettings}"

    val kotlinxCoroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Version.coroutines}"
    val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutines}"

    val androidXDataStorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
    val androidXActivity = "androidx.activity:activity:1.4.0"
    val androidXActivityCompose = "androidx.activity:activity-compose:1.4.0"

    val decompose = "com.arkivanov.decompose:decompose:${Version.decompose}"
    val essenty = "com.arkivanov.essenty:lifecycle:${Version.essenty}"
    val stateKeeper = "com.arkivanov.essenty:state-keeper:${Version.essenty}"
    val decomposeExtensions = "com.arkivanov.decompose:extensions-compose:${Version.decompose}"

    val composeUtil = "in.procyk.compose:util:1.5.11.0"

    val parcelizeDarwinRuntime = "com.arkivanov.parcelize.darwin:runtime:${Version.parcelizeDarwinVersion}"

    val exposedCore = "org.jetbrains.exposed:exposed-core:${Version.exposed}"
    val exposedDao = "org.jetbrains.exposed:exposed-dao:${Version.exposed}"
    val exposedJdbc = "org.jetbrains.exposed:exposed-jdbc:${Version.exposed}"
    val exposedJavaTime = "org.jetbrains.exposed:exposed-java-time:${Version.exposed}"

    val postgresSqlDriver = "org.postgresql:postgresql:42.7.1"
}
