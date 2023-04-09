package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import ml.dev.kotlin.minigames.shared.util.BlockV2Set
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.random

private const val SPEED_NORM: Float = 0.1f
private const val SIZE_PART: Float = 0.33f
private const val RECREATE_AFTER_MILLIS: Long = 5_000

private inline val V2.speed get() = normedTo(SPEED_NORM)

@Serializable
data class SnakePart(val pos: V2) {
    fun update(afterMillis: Long, v: V2): SnakePart = copy(pos = pos + v * afterMillis)
}

@Serializable
data class SnakeDirection(val dir: V2)

@Serializable
data class Snake constructor(
        val parts: List<SnakePart>,
        val v: V2,
        val size: Float,
) {
    val head: SnakePart? get() = parts.firstOrNull()
    val radius: Float get() = size / 2

    fun update(afterMillis: Long): Snake = parts.mapIndexed { i, part ->
        val v = if (i == 0) v else {
            val diff = parts[i - 1].pos - parts[i].pos
            if (diff.len >= size * SIZE_PART) diff.speed else V2.ZERO
        }
        part.update(afterMillis, v)
    }.let { copy(parts = it) }

    fun setDirection(direction: SnakeDirection): Snake = copy(v = direction.dir.speed)

    companion object {
        fun create(
                parts: Int = SNAKE_INIT_PARTS,
                size: Float = SNAKE_INIT_SIZE,
                at: V2 = V2.random(-DROP_SNAKE_RADIUS..DROP_SNAKE_RADIUS, -DROP_SNAKE_RADIUS..DROP_SNAKE_RADIUS),
        ): Snake = Snake(
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
) : GameSnapshot

@Serializable
data class SnakeGameUpdate(
        private val dir: SnakeDirection,
) : GameUpdate {

    override fun update(forUser: Username, gameState: GameState, currMillis: Long): GameState = when (gameState) {
        !is SnakeGameState -> gameState
        else -> {
            val snakeUpdate = gameState.snakes[forUser]?.takeAlive()
                    ?.setDirection(dir)?.let { forUser to SnakeState.Alive(it) }
            val updatedSnakes = snakeUpdate?.let { gameState.snakes + it } ?: gameState.snakes
            gameState.copy(snakes = updatedSnakes)
        }
    }
}

data class SnakeGameState(
        val snakes: Map<Username, SnakeState>,
        val items: BlockV2Set,
        override val points: Map<Username, Int>,
        override val users: Map<Username, UserData>,
        val prevMillis: Long?,
) : GameState() {

    override fun updateWith(users: Map<Username, UserData>, points: Map<Username, Int>): SnakeGameState =
            copy(users = users, points = points)

    override fun snapshot(forUser: Username): SnakeGameSnapshot {
        val items = snakes[forUser]?.takeAlive()?.head
                ?.let { SnakePartRange(it.pos, SNAKE_SNAPSHOT_SIZE) }
                ?.let { items[it.rangeX, it.rangeY] }
                ?: items.values
        val snakes = buildMap { snakes.forEach { (username, state) -> state.takeAlive()?.let { put(username, it) } } }
        return SnakeGameSnapshot(snakes, items, points, users)
    }

    override fun snapshot(): CumulativeGameSnapshot =
            CumulativeGameSnapshot.DifferentForEachUser(users.keys.associateWith { snapshot(it) })

    override fun addUser(username: Username, role: UserRole): GameState {
        val superGameState = super.addUser(username, role)
        val gameState = (superGameState as? SnakeGameState) ?: return superGameState
        val snake = gameState.snakes[username] ?: Snake.create().alive()
        val updatedSnakes = gameState.snakes + (username to snake)
        return gameState.copy(snakes = updatedSnakes)
    }

    override fun removeUser(username: Username): GameState {
        val superGameState = super.removeUser(username)
        val gameState = (superGameState as? SnakeGameState) ?: return superGameState
        val updatedSnakes = gameState.snakes - username
        return gameState.copy(snakes = updatedSnakes)
    }

    override fun update(currMillis: Long): SnakeGameState = move(currMillis)
            .dropItems()
            .collectItems()
            .updateSnakesSizes()
            .collideSnakes(currMillis)

    private fun move(currMillis: Long): SnakeGameState {
        val afterMillis = prevMillis?.let { currMillis - it } ?: return copy(prevMillis = currMillis)
        val updatedSnakes = snakes.mapValues { it.value.mapAlive(currMillis) { snake -> snake.update(afterMillis) } }
        return copy(snakes = updatedSnakes, prevMillis = currMillis)
    }

    private fun dropItems(): SnakeGameState {
        val expectInRange = (0..SNAKE_RANGE_MAX_COUNT).random()
        var currItems = items
        for ((_, snake) in snakes) {
            val head = snake.takeAlive()?.head?.let { SnakePartRange(it.pos, radius = DROP_ITEMS_RADIUS) } ?: continue
            val dropCount = expectInRange - currItems[head.rangeX, head.rangeY].size
            if (dropCount <= 0) continue
            currItems = HashSet<V2>().apply {
                for (i in 1..dropCount) add(V2(head.rangeX.random(), head.rangeY.random()))
            }.let { currItems.addAll(it) }
        }
        return copy(items = currItems)
    }

    private fun collectItems(): SnakeGameState {
        val pointsCopy = HashMap(points)
        var currItems = items
        for ((username, snakeState) in snakes) {
            val snake = snakeState.takeAlive() ?: continue
            val head = snake.head?.let { SnakePartRange(it.pos, radius = snake.radius) } ?: continue
            val userItems = currItems[head.rangeX, head.rangeY].filter(head::inRange)
            currItems = currItems.removeAll(userItems)
            pointsCopy[username] = (pointsCopy[username] ?: 0) + userItems.size
        }
        return copy(items = currItems, points = pointsCopy)
    }

    private fun collideSnakes(currMillis: Long): SnakeGameState {
        val snakesCopy = HashMap(snakes)
        var currItems = items
        val partsUsers = ComputedMap<SnakePartRange, HashSet<Username>> { HashSet() }.apply {
            for ((username, snakeState) in snakes) snakeState.takeAlive()?.let { snake ->
                snake.parts.forEach { this[SnakePartRange(it.pos, radius = snake.radius)] += username }
            }
        }
        val parts = partsUsers.keys.toSet()

        for ((username, snakeState) in snakes) {
            val snake = snakeState.takeAlive() ?: continue
            val head = snake.head?.let { SnakePartRange(it.pos, radius = snake.radius) } ?: continue
            val dead = parts.any { head.collides(it) && username !in partsUsers[it] }
            if (dead) {
                snakesCopy[username] = SnakeState.Dead(recreateAt = currMillis + RECREATE_AFTER_MILLIS)
                val r = snake.radius
                currItems = snake.parts.map { it.pos + V2.random(-r..r, -r..r) }.let(currItems::addAll)
            }
        }
        return copy(snakes = snakesCopy, items = currItems)
    }

    private fun updateSnakesSizes(): SnakeGameState {
        val snakesCopy = HashMap(snakes)
        for ((username, snakeState) in snakes) {
            val snake = snakeState.takeAlive() ?: continue
            val points = points[username] ?: 0

            val expectedParts = SNAKE_INIT_PARTS + (points / SNAKE_POINTS_FOR_PART)
            val parts = if (snake.parts.size >= expectedParts) snake.parts else {
                val lastPart = snake.parts.last()
                buildList {
                    addAll(snake.parts)
                    repeat(expectedParts - snake.parts.size) { add(lastPart) }
                }
            }

            val expectedSize = SNAKE_INIT_SIZE + (points / SNAKE_POINTS_FOR_GROW)
            val size = if (snake.size >= expectedSize) snake.size else expectedSize

            snakesCopy[username] = SnakeState.Alive(snake.copy(parts = parts, size = size))
        }
        return copy(snakes = snakesCopy)
    }

    companion object {
        fun empty(): SnakeGameState = SnakeGameState(
                snakes = emptyMap(),
                points = emptyMap(),
                users = emptyMap(),
                items = BlockV2Set(3 * DROP_ITEMS_RADIUS),
                prevMillis = null
        )
    }
}

private data class SnakePartRange(val pos: V2, val radius: Float) {
    val rangeX = pos.x - radius..pos.x + radius
    val rangeY = pos.y - radius..pos.y + radius
    fun inRange(v: V2): Boolean = (pos - v).len <= radius
    fun collides(other: SnakePartRange): Boolean = (pos - other.pos).len <= (radius + other.radius)
}

private const val DROP_ITEMS_RADIUS: Float = 512f
private const val DROP_SNAKE_RADIUS: Float = 256f
private const val SNAKE_SNAPSHOT_SIZE: Float = 1024f
private const val SNAKE_RANGE_MAX_COUNT: Int = 16
private const val SNAKE_POINTS_FOR_PART: Int = 16
private const val SNAKE_POINTS_FOR_GROW: Int = 32
private const val SNAKE_INIT_PARTS: Int = 16
private const val SNAKE_INIT_SIZE: Float = 32f

sealed class SnakeState {
    data class Dead(val recreateAt: Long) : SnakeState()
    data class Alive(val snake: Snake) : SnakeState()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun SnakeState.takeAlive(): Snake? = if (this is SnakeState.Alive) snake else null

private inline fun SnakeState.mapAlive(currMillis: Long, f: (Snake) -> Snake): SnakeState = when {
    this is SnakeState.Alive -> f(snake).alive()
    this is SnakeState.Dead && currMillis >= recreateAt -> Snake.create().alive()
    else -> this
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Snake.alive(): SnakeState.Alive = SnakeState.Alive(this)

