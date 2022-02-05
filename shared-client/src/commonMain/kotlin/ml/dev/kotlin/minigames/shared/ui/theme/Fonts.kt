package ml.dev.kotlin.minigames.shared.ui.theme


import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

object Fonts {
  @Composable
  fun leckerliOne() = FontFamily(
    Font(
      "LeckerliOne",
      "leckerlione_regular",
      FontWeight.Normal,
      FontStyle.Normal,
    ),
  )
}

@Composable
expect fun Font(name: String, res: String, weight: FontWeight, style: FontStyle): Font
