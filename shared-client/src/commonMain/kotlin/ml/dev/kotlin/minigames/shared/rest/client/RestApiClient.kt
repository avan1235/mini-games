package ml.dev.kotlin.minigames.shared.rest.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import ml.dev.kotlin.minigames.shared.rest.RestApiConfig
import ml.dev.kotlin.minigames.shared.util.*

class RestApiClient : Closeable {

    @OptIn(ExperimentalSerializationApi::class)
    val httpClient = HttpClient(CLIENT_ENGINE_FACTORY) {
        install(ContentNegotiation) { cbor() }
        followRedirects = true
        expectSuccess = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified E, reified T> post(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Res<E, T>? = try {
        httpClient.post {
            url {
                protocol = URLProtocol.byName[RestApiConfig.scheme]!!
                host = RestApiConfig.host
                contentType(ContentType.Application.Cbor)
                path(path)
            }
            block()
        }
            .body<T>()
            .ok()
    } catch (e: ClientRequestException) {
        tryOrNull {
            val errorData = e.response.readBytes()
            GameSerialization.decodeFromByteArray<E>(errorData).err()
        }
    } catch (_: Exception) {
        null
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified E, reified T> get(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Res<E, T>? = try {
        httpClient.get {
            url {
                protocol = URLProtocol.byName[RestApiConfig.scheme]!!
                host = RestApiConfig.host
                contentType(ContentType.Application.Cbor)
                path(path)
            }
            block()
        }
            .body<T>()
            .ok()
    } catch (e: ClientRequestException) {
        tryOrNull {
            val errorData = e.response.readBytes()
            GameSerialization.decodeFromByteArray<E>(errorData).err()
        }
    } catch (_: Exception) {
        null
    }

    override fun close(): Unit = httpClient.close()
}

internal expect val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*>
