package ml.dev.kotlin.minigames.util

import ml.dev.kotlin.minigames.shared.util.FloatRange
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.V2Set
import java.util.*

class TreeV2Set(
  private val byX: TreeMap<Float, HashSet<V2>> = TreeMap(),
  private val byY: TreeMap<Float, HashSet<V2>> = TreeMap(),
) : V2Set {
  override val values: Set<V2> get() = byX.values.flatMapTo(HashSet()) { it }

  override fun get(x: FloatRange, y: FloatRange): Set<V2> {
    val xs = byX.subMap(x.start, true, x.endInclusive, true).flatMapTo(HashSet()) { it.value }
    val ys = byY.subMap(y.start, true, y.endInclusive, true).flatMapTo(HashSet()) { it.value }
    return xs.intersect(ys)
  }

  override fun addAll(elements: Iterable<V2>): TreeV2Set {
    val byXCopy = TreeMap(byX)
    val byYCopy = TreeMap(byY)
    elements.forEach {
      byXCopy.computeIfAbsent(it.x) { HashSet() } += it
      byYCopy.computeIfAbsent(it.y) { HashSet() } += it
    }
    return TreeV2Set(byXCopy, byYCopy)
  }

  override fun removeAll(elements: Iterable<V2>): TreeV2Set {
    val byXCopy = TreeMap(byX)
    val byYCopy = TreeMap(byY)
    elements.forEach {
      byXCopy[it.x]?.remove(it)
      byYCopy[it.y]?.remove(it)
    }
    return TreeV2Set(byXCopy, byYCopy)
  }
}
