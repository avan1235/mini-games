import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.lang.System.getenv

private const val ENV_FILE: String = ".env"
private const val FROM_APP_ENV_FILE: String = "../.env"
private const val FROM_XCODE_ENV_FILE: String = "../../.env"

class BuildSrcPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.withType(JavaExec::class.java) {
            ENV.forEach { (k, v) -> environment(k, v) }
        }
    }
}

private fun currentScopeEnvFile(): File =
    File(ENV_FILE).takeIf { it.exists() } ?: File(FROM_APP_ENV_FILE).takeIf { it.exists() }  ?: File(FROM_XCODE_ENV_FILE)

val ENV: Map<String, String>
    get() = currentScopeEnvFile().readLines().mapNotNull { line ->
        val idx = line.indexOf('=').takeIf { it > -1 } ?: return@mapNotNull null
        line.substring(0 until idx) to line.substring(idx + 1)
    }.toMap().let { it + getenv() }

val VERSION: String get() = ENV["VERSION"] ?: "1.0.0"

val CORS_PORT: Int get() = ENV["CORS_PORT"]?.toIntOrNull()!!

private val VERSION_CODE_PREFIX = "version.code="

val VERSION_CODE: Int
    get() = if (ENV["BUMP_FILE_VERSION_CODE"]?.toBoolean() == true) {
        val line = File("version.properties").readLines().first()
        val number = line.removePrefix(VERSION_CODE_PREFIX).toInt()
        number.also { File("version.properties").writeText("$VERSION_CODE_PREFIX${it + 1}") }
    } else 1
