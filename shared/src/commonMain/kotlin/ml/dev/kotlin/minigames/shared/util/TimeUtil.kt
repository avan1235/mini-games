package ml.dev.kotlin.minigames.shared.util

import kotlinx.datetime.Clock

fun now(): Long = Clock.System.now().toEpochMilliseconds()
