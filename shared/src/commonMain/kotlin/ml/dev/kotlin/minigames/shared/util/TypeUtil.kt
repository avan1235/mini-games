package ml.dev.kotlin.minigames.shared.util

import kotlin.random.Random

inline fun <T : Any> tryOrNull(action: () -> T): T? = try {
  action()
} catch (_: Exception) {
  null
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
inline fun <T> Any.cast(): T = this as T

@Suppress("NOTHING_TO_INLINE")
inline fun Any?.unit() = Unit

inline fun <reified T> Any.takeTyped(): T? = takeIf { it is T }?.cast()

interface Named {
  val name: String
}

typealias FloatRange = ClosedFloatingPointRange<Float>

@Suppress("NOTHING_TO_INLINE")
inline fun FloatRange.random(): Float = start + Random.nextFloat() * (endInclusive - start)
