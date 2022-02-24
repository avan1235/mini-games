package ml.dev.kotlin.minigames.shared.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun now(): Long = Clock.System.now().toEpochMilliseconds()

fun Long.toDateTime(): Instant = Instant.fromEpochMilliseconds(this)
