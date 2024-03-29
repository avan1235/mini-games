@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.shared.util

inline fun <V> Set<V>.everyUnorderedTriple(action: (V, V, V) -> Unit) {
    val list = toList()
    for (i in 0 until list.size - 2) for (j in i + 1 until list.size - 1) for (k in j + 1 until list.size) action(
        list[i],
        list[j],
        list[k],
    )
}

inline fun <T, R, V> Array<out T>.zip(other1: List<R>, other2: List<V>): List<Triple<T, R, V>> {
    val arraySize = minOf(size, other1.count(), other2.count())
    val list = ArrayList<Triple<T, R, V>>(arraySize)
    for (idx in 0 until arraySize) {
        list.add(Triple(this[idx], other1[idx], other2[idx]))
    }
    return list
}

class ComputedMap<K, V>(
    private val map: MutableMap<K, V> = HashMap(),
    private val default: MutableMap<K, V>.(K) -> V,
) : MutableMap<K, V> by map {
    override fun get(key: K): V = map[key]?.let { return it } ?: default(key).also { map[key] = it }
    override fun remove(key: K): V = map.remove(key) ?: this.default(key)
    override fun toString(): String = "ComputedMap(map=$map)"
    override fun hashCode(): Int = map.hashCode()
    override fun equals(other: Any?): Boolean = (other as? ComputedMap<*, *>)?.let { it.map == map } ?: false
}

data class BlockV2Set(
    private val rangeUnit: Float,
    private val byRange: Map<BlockIdx, HashSet<V2>> = HashMap(),
) {
    val values: Set<V2> get() = byRange.values.flatMapTo(HashSet()) { it }

    operator fun get(x: FloatRange, y: FloatRange): Set<V2> {
        val xRng = (x.start ranged rangeUnit) spanned (x.endInclusive ranged rangeUnit)
        val yRng = (y.start ranged rangeUnit) spanned (y.endInclusive ranged rangeUnit)
        return buildSet {
            (xRng * yRng).forEach { idx ->
                byRange[idx]?.forEach { if (it.x in x && it.y in y) add(it) }
            }
        }
    }

    fun addAll(elements: Iterable<V2>): BlockV2Set {
        val byRangeCopy = HashMap(byRange)
        elements.forEach {
            val blockIdx = it ranged rangeUnit
            if (blockIdx !in byRangeCopy) {
                byRangeCopy[blockIdx] = HashSet()
            }
            byRangeCopy[blockIdx]!! += it
        }
        return copy(byRange = byRangeCopy)
    }

    fun removeAll(elements: Iterable<V2>): BlockV2Set {
        val byRangeCopy = HashMap(byRange)
        elements.forEach { byRangeCopy[it ranged rangeUnit]?.remove(it) }
        return copy(byRange = byRangeCopy)
    }
}

data class BlockIdx(val xIdx: Int, val yIdx: Int)

private operator fun IntRange.times(o: IntRange): List<BlockIdx> = buildList {
    for (x in this@times) for (y in o) add(BlockIdx(x, y))
}

private infix fun Float.ranged(rangeUnit: Float): Int =
    if (this >= 0) (this / rangeUnit).toInt() else (-this / rangeUnit).toInt()

private infix fun V2.ranged(rangeUnit: Float): BlockIdx = BlockIdx(this.x ranged rangeUnit, this.y ranged rangeUnit)

private infix fun Int.spanned(o: Int): IntRange = if (this <= o) this..o else o..this
