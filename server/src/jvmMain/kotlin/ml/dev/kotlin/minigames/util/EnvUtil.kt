@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.util

inline fun <reified T> envVar(name: String): T = when (T::class) {
    Int::class -> System.getenv(name)?.toInt() as? T
    String::class -> System.getenv(name) as? T
    Boolean::class -> System.getenv(name)?.toBoolean() as? T
    else -> throw IllegalStateException("Getting env variables for ${T::class} not defined")
} ?: throw IllegalArgumentException("Env variable $name not defined")

@Suppress("NOTHING_TO_INLINE")
inline fun eprintln(s: Any?) = System.err.println(s)
