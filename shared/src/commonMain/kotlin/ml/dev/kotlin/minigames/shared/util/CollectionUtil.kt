package ml.dev.kotlin.minigames.shared.util

inline fun <V> Set<V>.everyUnorderedTriple(action: (V, V, V) -> Unit) {
  val list = toList()
  for (i in 0 until list.size - 2) for (j in i + 1 until list.size - 1) for (k in j + 1 until list.size) action(
    list[i],
    list[j],
    list[k],
  )
}

class ComputedMap<K, V>(
  private val map: MutableMap<K, V> = HashMap(),
  private val default: (K) -> V
) {
  operator fun get(key: K): V = map[key] ?: default(key).also { map[key] = it }
  operator fun set(key: K, value: V): Unit = map.set(key, value)
  val keys: Set<K> get() = map.keys
}

interface V2Set {
  val values: Set<V2>

  fun get(x: FloatRange, y: FloatRange): Set<V2>
  fun addAll(elements: Iterable<V2>): V2Set
  fun removeAll(elements: Iterable<V2>): V2Set
}
