package ml.dev.kotlin.minigames.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
actual fun Font(name: String, res: String, weight: FontWeight, style: FontStyle): Font {
  val context = LocalContext.current
  val id = context.resources.getIdentifier(res, "font", context.packageName)
  return Font(id, weight, style)
}
