object Dependencies {

    object Version {
        val kotlinVersion by System.getProperties()
        val composeVersion by System.getProperties()
        val parcelizeDarwinVersion by System.getProperties()
        const val ktor = "2.2.4"
        const val exposed = "0.41.1" // Keep this version compatible with the version of exposed used by krush
        const val krush = "1.2.0"
        const val simpleMail = "1.3.3"
        const val decompose = "0.5.0"
        const val essenty = "0.2.2"
        const val multiplatformSettings = "1.0.0"
    }

    val kotlinxSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.3.2"
    val kotlinxAtomicFu = "org.jetbrains.kotlinx:atomicfu:0.20.2"
    val kotlinxDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"
    val uuid = "com.benasher44:uuid:0.7.0"

    val ktorServerCore = "io.ktor:ktor-server-core:${Version.ktor}"
    val ktorServerNetty = "io.ktor:ktor-server-netty:${Version.ktor}"
    val ktorServerSerialization = "io.ktor:ktor-serialization-kotlinx-cbor:${Version.ktor}"
    val ktorServerWebsockets = "io.ktor:ktor-server-websockets:${Version.ktor}"
    val ktorServerContentNegotiation = "io.ktor:ktor-server-content-negotiation:${Version.ktor}"
    val ktorAuth = "io.ktor:ktor-server-auth:${Version.ktor}"
    val ktorAuthJwt = "io.ktor:ktor-server-auth-jwt:${Version.ktor}"
    val ktorHtmlBuilder = "io.ktor:ktor-server-html-builder:${Version.ktor}"
    val ktorClientSerialization = "io.ktor:ktor-serialization-kotlinx-cbor:${Version.ktor}"
    val ktorClientContentNegotiation = "io.ktor:ktor-client-content-negotiation:${Version.ktor}"
    val ktorClientCore = "io.ktor:ktor-client-core:${Version.ktor}"
    val ktorClientAndroid = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientDarwin = "io.ktor:ktor-client-darwin:${Version.ktor}"
    val ktorClientDesktop = "io.ktor:ktor-client-okhttp:${Version.ktor}"
    val ktorClientWebsockets = "io.ktor:ktor-client-websockets:${Version.ktor}"
    val logbackClassic = "ch.qos.logback:logback-classic:1.2.6"

    val simpleMailCore = "net.axay:simplekotlinmail-core:${Version.simpleMail}"
    val simpleMailClient = "net.axay:simplekotlinmail-client:${Version.simpleMail}"

    val bCrypt = "at.favre.lib:bcrypt:0.9.0"

    val napierLogger = "io.github.aakira:napier:2.4.0"

    val multiplatformSettings = "com.russhwolf:multiplatform-settings:${Version.multiplatformSettings}"
    val multiplatformSettingsCoroutines = "com.russhwolf:multiplatform-settings-coroutines:${Version.multiplatformSettings}"
    val multiplatformSettingsDatastore = "com.russhwolf:multiplatform-settings-datastore:${Version.multiplatformSettings}"

    val androidGoogleMaterial = "com.google.android.material:material:1.4.0"
    val androidXDataStorePreferences = "androidx.datastore:datastore-preferences:1.0.0"
    val androidXActivity = "androidx.activity:activity:1.4.0"
    val androidXActivityCompose = "androidx.activity:activity-compose:1.4.0"

    val decompose = "com.arkivanov.decompose:decompose:${Version.decompose}"

    val essentyParcelable = "com.arkivanov.essenty:parcelable:${Version.essenty}"
    val essentyLifecycle = "com.arkivanov.essenty:lifecycle:${Version.essenty}"
    val essentyStateKeeper = "com.arkivanov.essenty:state-keeper:${Version.essenty}"
    val essentyInstanceKeeper = "com.arkivanov.essenty:instance-keeper:${Version.essenty}"
    val parcelizeDarwinRuntime = "com.arkivanov.parcelize.darwin:runtime:${Version.parcelizeDarwinVersion}"

    val exposedCore = "org.jetbrains.exposed:exposed-core:${Version.exposed}"
    val exposedDao = "org.jetbrains.exposed:exposed-dao:${Version.exposed}"
    val exposedJdbc = "org.jetbrains.exposed:exposed-jdbc:${Version.exposed}"
    val exposedJavaTime = "org.jetbrains.exposed:exposed-java-time:${Version.exposed}"

    val postgresSqlDriver = "org.postgresql:postgresql:42.3.0"

    val krushAnnotationProcessor = "pl.touk.krush:krush-annotation-processor:${Version.krush}"
    val krushRuntime = "pl.touk.krush:krush-runtime:${Version.krush}"
    val krushRuntimePostgresql = "pl.touk.krush:krush-runtime-postgresql:${Version.krush}"
}
