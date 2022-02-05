package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.V2Set
import ml.dev.kotlin.minigames.shared.util.random

private const val SPEED_NORM: Float = 0.1f
private const val SIZE_PART: Float = 0.33f

private inline val V2.speed get() = normedTo(SPEED_NORM)

@Serializable
data class SnakePart(val pos: V2) {
  fun update(afterMillis: Long, v: V2): SnakePart = copy(pos = pos + v * afterMillis)
}

@Serializable
data class SnakeDirection(val dir: V2)

@Serializable
data class Snake(
  val parts: List<SnakePart>,
  val v: V2,
  val size: Float,
) {
  val head: SnakePart? get() = parts.firstOrNull()

  fun update(afterMillis: Long): Snake = parts.mapIndexed { i, part ->
    val v = if (i == 0) v else {
      val diff = parts[i - 1].pos - parts[i].pos
      if (diff.len >= size * SIZE_PART) diff.speed else V2.ZERO
    }
    part.update(afterMillis, v)
  }.let { copy(parts = it) }

  fun setDirection(direction: SnakeDirection): Snake = copy(v = direction.dir.speed)

  companion object {
    fun create(at: V2 = V2.ZERO, parts: Int = 20, size: Float = 32f): Snake = Snake(
      parts = List(parts) { SnakePart(at + V2(0f, -size * SIZE_PART * it)) },
      v = V2.ZERO_ONE.speed,
      size = size,
    )
  }
}

@Serializable
data class SnakeGameSnapshot(
  val snakes: Map<Username, Snake>,
  val items: Set<V2>,
  override val points: Map<Username, Int>,
  override val users: Map<Username, UserData>,
) : GameSnapshot()

@Serializable
data class SnakeGameUpdate(
  private val dir: SnakeDirection,
) : GameUpdate() {

  override fun update(forUser: Username, gameState: GameState): GameState = when (gameState) {
    !is SnakeGameState -> gameState
    else -> {
      val snakeUpdate = gameState.snakes[forUser]?.setDirection(dir)?.let { forUser to it }
      val updatedSnakes = snakeUpdate?.let { gameState.snakes + it } ?: gameState.snakes
      gameState.copy(snakes = updatedSnakes)
    }
  }
}

data class SnakeGameState(
  val snakes: Map<Username, Snake>,
  val items: V2Set,
  override val points: Map<Username, Int>,
  override val users: Map<Username, UserData>,
  val prevMillis: Long?,
) : GameState() {

  override fun updateWith(users: Map<Username, UserData>, points: Map<Username, Int>): SnakeGameState =
    copy(users = users, points = points)

  override fun snapshot(forUser: Username): SnakeGameSnapshot {
    val items = snakes[forUser]?.head
      ?.let { SnakeHeadRange(it.pos, SNAPSHOT_RADIUS) }
      ?.let { items.get(it.rangeX, it.rangeY) }
      ?: items.values
    return SnakeGameSnapshot(snakes, items, points, users)
  }

  override fun addUser(username: Username, role: UserRole): GameState {
    val superGameState = super.addUser(username, role)
    val gameState = (superGameState as? SnakeGameState) ?: return superGameState
    val snake = gameState.snakes[username] ?: Snake.create()
    val updatedSnakes = gameState.snakes + (username to snake)
    return gameState.copy(snakes = updatedSnakes)
  }

  override fun update(currMillis: Long): SnakeGameState = move(currMillis).dropItems().collectItems()

  private fun move(currMillis: Long): SnakeGameState {
    val afterMillis = prevMillis?.let { currMillis - it } ?: return copy(prevMillis = currMillis)
    val updatedSnakes = snakes.mapValues { it.value.update(afterMillis) }
    return copy(snakes = updatedSnakes, prevMillis = currMillis)
  }

  private fun dropItems(): SnakeGameState {
    val expectInRange = (0..RANGE_MAX_COUNT).random()
    var items = items
    for (snake in snakes.values) {
      val head = snake.head?.let { SnakeHeadRange(it.pos, radius = DROP_RADIUS) } ?: continue
      val dropCount = expectInRange - items.get(head.rangeX, head.rangeY).size
      if (dropCount <= 0) continue
      items = HashSet<V2>().apply {
        for (i in 1..dropCount) add(V2(head.rangeX.random(), head.rangeY.random()))
      }.let { items.addAll(it) }
    }
    return copy(items = items)
  }

  private fun collectItems(): SnakeGameState {
    var items = items
    val points = HashMap(points)
    for ((username, snake) in snakes) {
      val head = snake.head?.let { SnakeHeadRange(it.pos, radius = snake.size / 2) } ?: continue
      val userItems = items.get(head.rangeX, head.rangeY).filter(head::inRange)
      items = items.removeAll(userItems)
      points[username] = (points[username] ?: 0) + userItems.size
    }
    return copy(items = items, points = points)
  }

  companion object {
    fun empty(items: V2Set): SnakeGameState = SnakeGameState(
      snakes = emptyMap(), points = emptyMap(), users = emptyMap(), items = items, prevMillis = null
    )
  }
}

private data class SnakeHeadRange(val pos: V2, val radius: Float) {
  val rangeX = pos.x - radius..pos.x + radius
  val rangeY = pos.y - radius..pos.y + radius
  fun inRange(v: V2): Boolean = (pos - v).len <= radius
}

private const val DROP_RADIUS: Float = 512f
private const val SNAPSHOT_RADIUS: Float = 512f
private const val RANGE_MAX_COUNT: Int = 16
