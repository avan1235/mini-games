package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BlueColor: Color = Color(0xFF007AFF)
val GreenColor: Color = Color(0xFF33CC3E)
val PurpleColor: Color = Color(0xFF635FC7)
val PinkColor: Color = Color(0xFFFF0076)

val DiscardColor: Color = Color(0xFFEE675C)
val ApproveColor: Color = Color(0xFF8AB4F8)

@Composable
fun Theme(content: @Composable () -> Unit) {
  val themeColors = darkColors(
    onPrimary = Color(0xFFFFFFFF),
    primary = Color(0xFFCDCDCD),
    primaryVariant = Color(0xFF222222),

    onSecondary = Color(0xFFFFFFFF),
    secondary = Color(0xFFBDBDBD),
    secondaryVariant = Color(0xFF111111),

    onBackground = Color(0xFFFFFFFF),
    background = Color(0xFF212121),

    onSurface = Color(0xFFFFFFFF),
    surface = Color(0xFF353535),

    onError = Color(0xFF000000),
    error = Color(0xFFFF3C3C),
  )

  MaterialTheme(
    colors = themeColors,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}
