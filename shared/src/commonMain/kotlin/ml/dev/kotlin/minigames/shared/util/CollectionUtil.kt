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
) : MutableMap<K, V> by map {
  override fun get(key: K): V = map.getOrDefault(key, default(key)).also { this[key] = it }
  override fun remove(key: K): V = map.remove(key) ?: default(key)
  override fun toString(): String = "ComputedMap(map=$map)"
  override fun hashCode(): Int = map.hashCode()
  override fun equals(other: Any?): Boolean = (other as? ComputedMap<*, *>)?.let { it.map == map } ?: false
}

interface V2Set {
  val values: Set<V2>

  fun get(x: FloatRange, y: FloatRange): Set<V2>
  fun addAll(elements: Iterable<V2>): V2Set
  fun removeAll(elements: Iterable<V2>): V2Set
}
