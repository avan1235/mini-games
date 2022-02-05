package ml.dev.kotlin.minigames.shared.rest.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.decodeFromString
import ml.dev.kotlin.minigames.shared.rest.RestApiConfig
import ml.dev.kotlin.minigames.shared.util.*

class RestJsonApiClient : Closeable {

  val httpClient = HttpClient(CLIENT_ENGINE_FACTORY) {
    install(JsonFeature) {
      serializer = KotlinxSerializer()
    }
    followRedirects = true
  }

  suspend inline fun <reified E, reified T> post(
    path: String,
    block: HttpRequestBuilder.() -> Unit = {}
  ): Res<E, T>? = try {
    httpClient.post<T>(
      scheme = RestApiConfig.scheme,
      host = RestApiConfig.host,
      path = path,
    ) {
      contentType(ContentType.Application.Json)
      block()
    }.ok()
  } catch (e: ClientRequestException) {
    tryOrNull {
      val errorData = e.response.readText()
      GameJson.decodeFromString<E>(errorData).err()
    }
  } catch (_: Exception) {
    null
  }

  suspend inline fun <reified E, reified T> get(
    path: String,
    block: HttpRequestBuilder.() -> Unit = {}
  ): Res<E, T>? = try {
    httpClient.get(
      scheme = RestApiConfig.scheme,
      host = RestApiConfig.host,
      path = path,
    ) {
      contentType(ContentType.Application.Json)
      block()
    }
  } catch (e: ClientRequestException) {
    tryOrNull {
      val errorData = e.response.readText()
      GameJson.decodeFromString<E>(errorData).err()
    }
  } catch (_: Exception) {
    null
  }

  override fun close(): Unit = httpClient.close()
}

internal expect val CLIENT_ENGINE_FACTORY: HttpClientEngineFactory<*>
