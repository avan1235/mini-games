package ml.dev.kotlin.minigames.shared.util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual fun initClientLogger() {
  Napier.base(DebugAntilog())
}
