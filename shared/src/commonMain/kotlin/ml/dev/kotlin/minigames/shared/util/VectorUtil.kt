package ml.dev.kotlin.minigames.shared.util

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

@Serializable
data class V2(val x: Float, val y: Float) {

    infix fun rad(o: V2): Float = acos((this * o) / (len * o.len))
    infix fun deg(o: V2): Float = rad(o) * 180f / PI.toFloat()
    operator fun plus(o: V2): V2 = V2(x + o.x, y + o.y)
    operator fun times(o: V2): Float = x * o.x + y * o.y
    operator fun minus(o: V2): V2 = V2(x - o.x, y - o.y)
    operator fun div(o: Float): V2 = V2(x / o, y / o)
    operator fun times(o: Float): V2 = V2(x * o, y * o)
    operator fun times(o: Long): V2 = V2(x * o, y * o)
    fun normedTo(expectedLength: Float): V2 {
        val expectedLength2 = expectedLength * expectedLength
        val l2 = x * x + y * y
        val scale = sqrt(expectedLength2 / l2)
        return V2(x * scale, y * scale)
    }

    val len2: Float get() = x * x + y * y
    val len: Float get() = sqrt(len2)

    companion object {
        val ZERO: V2 = V2(x = 0f, y = 0f)
        val ONE: V2 = V2(x = 1f, y = 1f)
        val ZERO_ONE: V2 = V2(x = 0f, y = 1f)
        val ONE_ZERO: V2 = V2(x = 1f, y = 0f)
        fun random(xRange: FloatRange, yRange: FloatRange): V2 = V2(x = xRange.random(), y = yRange.random())
    }
}
