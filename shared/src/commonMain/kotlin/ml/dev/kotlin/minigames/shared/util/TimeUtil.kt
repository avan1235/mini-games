package ml.dev.kotlin.minigames.shared.util

import kotlinx.datetime.*

fun now(): Long = Clock.System.now().toEpochMilliseconds()

fun Long.format(format: DateTimeHolder.() -> String): String = Instant
  .fromEpochMilliseconds(this)
  .toLocalDateTime(TimeZone.currentSystemDefault())
  .run { DateTimeHolder(year, monthNumber, month, dayOfMonth, dayOfWeek, dayOfYear, hour, minute, second, nanosecond) }
  .let(format)

fun Int.toPaddedString(): String = toString().padStart(2, '0')

data class DateTimeHolder(
  val year: Int,
  val monthNumber: Int,
  val month: Month,
  val dayOfMonth: Int,
  val dayOfWeek: DayOfWeek,
  val dayOfYear: Int,
  val hour: Int,
  val minute: Int,
  val second: Int,
  val nanosecond: Int,
)

