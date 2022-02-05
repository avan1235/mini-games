import java.io.File
import java.lang.System.getenv
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.tasks.JavaExec

private const val ENV_FILE: String = ".env"

class BuildSrcPlugin : Plugin<Project> {
  override fun apply(target: Project) = Unit
}

fun Project.kapt(dependency: String) {
  val (group, name, version) = dependency.split(":")
  val moduleDependency = DefaultExternalModuleDependency(group, name, version)
  configurations.getByName("kapt").dependencies.add(moduleDependency)
}

fun Project.loadEnv() {
  tasks.withType(JavaExec::class.java) {
    ENV.forEach { (k, v) -> environment(k, v) }
  }
}

val ENV: Map<String, String> = File(ENV_FILE).readLines().mapNotNull { line ->
  val idx = line.indexOf('=').takeIf { it > -1 } ?: return@mapNotNull null
  line.substring(0 until idx) to line.substring(idx + 1)
}.toMap().let { it + getenv() }
