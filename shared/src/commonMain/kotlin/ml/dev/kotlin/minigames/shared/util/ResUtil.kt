@file:Suppress("NOTHING_TO_INLINE")

package ml.dev.kotlin.minigames.shared.util

sealed class Res<out ERROR, out OK> {
  class Ok<OK>(val value: OK) : Res<Nothing, OK>()
  class Err<ERROR>(val value: ERROR) : Res<ERROR, Nothing>()

  fun <R> map(f: (OK) -> R): Res<ERROR, R> = when (this) {
    is Ok -> f(value).ok()
    is Err -> value.err()
  }
}

inline fun <E, R> R.ok(): Res<E, R> = Res.Ok(this)

inline fun <E, R> E.err(): Res<E, R> = Res.Err(this)

inline fun <E, R, M> Res<E, R>?.on(
  ok: (R) -> M,
  err: (E) -> M,
  empty: () -> M = { throw IllegalStateException("Unexpected null value") },
): M = when (this) {
  is Res.Ok -> ok(value)
  is Res.Err -> err(value)
  null -> empty()
}

inline fun <M> Boolean.on(ok: () -> M, err: () -> M) = when (this) {
  true -> ok()
  false -> err()
}
