package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs

fun randomColor(): Color {
    val baseColor = Color.White.convert(ColorSpaces.Srgb)
    val baseRed = baseColor.red
    val baseGreen = baseColor.green
    val baseBlue = baseColor.blue
    fun pick(): Float = (0..255).random() / 255f
    val red = (baseRed + pick()) / 2
    val green = (baseGreen + pick()) / 2
    val blue = (baseBlue + pick()) / 2
    return Color(red, green, blue, colorSpace = ColorSpaces.Srgb)
}

fun Color.lightScaled(lightScale: Float): Color {
    val number = toHSL()
    return Color.hsl(number[0], number[1], number[2] * lightScale, alpha)
}

inline val Color.alphaArgb: Int get() = toArgb() ushr 24
inline val Color.redArgb: Int get() = toArgb() shr 16 and 0xFF
inline val Color.greenArgb: Int get() = toArgb() shr 8 and 0xFF
inline val Color.blueArgb: Int get() = toArgb() and 0xFF

fun Color.toHSL(): FloatArray = rgbToHSL(redArgb, greenArgb, blueArgb)

private fun rgbToHSL(r: Int, g: Int, b: Int): FloatArray = floatArrayOf(0f, 0f, 0f).apply {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    val max = rf.coerceAtLeast(gf.coerceAtLeast(bf))
    val min = rf.coerceAtMost(gf.coerceAtMost(bf))
    val deltaMaxMin = max - min
    var h: Float
    val s: Float
    val l = (max + min) / 2f
    if (max == min) {
        s = 0f
        h = s
    } else {
        h = when (max) {
            rf -> (gf - bf) / deltaMaxMin % 6f
            gf -> (bf - rf) / deltaMaxMin + 2f
            else -> (rf - gf) / deltaMaxMin + 4f
        }
        s = deltaMaxMin / (1f - abs(2f * l - 1f))
    }
    h = h * 60f % 360f
    if (h < 0) h += 360f

    this[0] = h.coerceIn(0f, 360f)
    this[1] = s.coerceIn(0f, 1f)
    this[2] = l.coerceIn(0f, 1f)
}
