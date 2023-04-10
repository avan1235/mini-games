package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.util.random
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextFloat

private const val MAX_POS_X: Float = 1f
private const val MAX_POS_Y: Float = 1f
private const val TOUCH_VEL_Y: Float = 0.00042f
private const val MAX_VEL_Y: Float = 0.03f
private const val DELTA_POS: Float = 1e-6f
private const val COLLECT_CANDY_DISTANCE_SQUARED: Float = 0.01f
private const val DIE_FROM_SPIKE_DISTANCE_SQUARED: Float = 0.007f
private val DIE_FROM_SPIKE_DISTANCE: Float = sqrt(DIE_FROM_SPIKE_DISTANCE_SQUARED)
private const val RECREATE_AFTER_MILLIS: Long = 3_000
private val START_VEL: V2 = V2.ONE_ZERO * 0.0012f
private val START_ACCEL: V2 = V2.ZERO_ONE * -0.0000006f
private const val MAX_ITEMS_COUNT: Int = 3
private const val ITEM_DROP_EVERY: Int = 3000
private const val MAX_SPIKES_COUNT: Int = 4

@Serializable
data class Bird(
    val pos: V2,
    val vel: V2,
    val acc: V2,
) {
    fun update(afterMillis: Long, revertVelocity: Boolean = false): Bird {
        val vel = if (revertVelocity) vel.copy(y = TOUCH_VEL_Y) else vel
        val pos = pos + vel * afterMillis + acc * (0.5f * afterMillis * afterMillis)
        val v = if (pos.x > MAX_POS_X || pos.x < -MAX_POS_X) vel.copy(x = -vel.x) else vel
        val coercedPos = pos
            .coerceAtLeast(minX = -MAX_POS_X + DELTA_POS, minY = -MAX_POS_X + DELTA_POS)
            .coerceAtMost(maxX = MAX_POS_Y - DELTA_POS, maxY = MAX_POS_Y - DELTA_POS)
        val coercedV = (v + acc * afterMillis)
            .coerceAtLeast(minY = -MAX_VEL_Y)
            .coerceAtMost(maxY = MAX_VEL_Y)
        return copy(pos = coercedPos, vel = coercedV)
    }

    companion object {
        fun create(): Bird = Bird(
            pos = V2.ZERO,
            vel = START_VEL,
            acc = START_ACCEL,
        )
    }
}

@Serializable
data class BirdGameSnapshot(
    val birds: Map<Username, Bird>,
    val candies: List<V2>,
    val spikes: List<V2>,
    override val points: Map<Username, Int>,
    override val users: Map<Username, UserData>,
) : GameSnapshot

@Serializable
object BirdGameUpdate : GameUpdate {

    override fun update(forUser: Username, gameState: GameState, currMillis: Long): GameState {
        if (gameState !is BirdGameState) return gameState
        val afterMillis =
            gameState.prevMillis?.let { currMillis - it } ?: return gameState.copy(prevMillis = currMillis)
        val birdUpdate = gameState.birds[forUser]?.takeAlive()
            ?.update(afterMillis, revertVelocity = true)?.let { forUser to it.alive() }
        val updatedBirds = birdUpdate?.let { gameState.birds + it } ?: gameState.birds
        return gameState.copy(birds = updatedBirds)
    }
}

data class BirdGameState(
    val birds: Map<Username, BirdState>,
    val candies: List<V2>,
    val leftSpikes: List<V2>,
    val rightSpikes: List<V2>,
    override val points: Map<Username, Int>,
    override val users: Map<Username, UserData>,
    val prevMillis: Long?,
    val changeLeftSpikes: Boolean = false,
    val changeRightSpikes: Boolean = false,
) : GameState() {

    override fun updateWith(users: Map<Username, UserData>, points: Map<Username, Int>): BirdGameState =
        copy(users = users, points = points)

    override fun snapshot(forUser: Username): BirdGameSnapshot =
        buildSnapshot()

    override fun snapshot(): CumulativeGameSnapshot =
        CumulativeGameSnapshot.SameForAllUsers(buildSnapshot())

    private fun buildSnapshot(): BirdGameSnapshot {
        val birds = buildMap { birds.forEach { (username, state) -> state.takeAlive()?.let { put(username, it) } } }
        return BirdGameSnapshot(birds, candies, leftSpikes + rightSpikes, points, users)
    }

    override fun addUser(username: Username, role: UserRole): GameState {
        val superGameState = super.addUser(username, role)
        val gameState = (superGameState as? BirdGameState) ?: return superGameState
        val bird = gameState.birds[username] ?: Bird.create().alive()
        val updatedBirds = gameState.birds + (username to bird)
        return gameState.copy(birds = updatedBirds)
    }

    override fun removeUser(username: Username): GameState {
        val superGameState = super.removeUser(username)
        val gameState = (superGameState as? BirdGameState) ?: return superGameState
        val updatedBirds = gameState.birds - username
        return gameState.copy(birds = updatedBirds)
    }

    override fun update(currMillis: Long): BirdGameState = move(currMillis)
        .collideBirds(currMillis)
        .changeSpikes()
        .dropItems()
        .collectItems()

    private fun move(currMillis: Long): BirdGameState {
        val afterMillis = prevMillis?.let { currMillis - it } ?: return copy(prevMillis = currMillis)
        var changeLeftSpikes = changeLeftSpikes
        var changeRightSpikes = changeRightSpikes
        val updatedBirds = birds.mapValues {
            it.value.mapAlive(currMillis) { bird ->
                val updatedBird = bird.update(afterMillis)
                if (bird.direction != updatedBird.direction) when (bird.direction) {
                    BirdDirection.Left -> changeLeftSpikes = true
                    BirdDirection.Right -> changeRightSpikes = true
                }
                updatedBird
            }
        }
        return copy(
            birds = updatedBirds,
            prevMillis = currMillis,
            changeLeftSpikes = changeLeftSpikes,
            changeRightSpikes = changeRightSpikes,
        )
    }

    private fun collideBirds(currMillis: Long): BirdGameState = copy(
        birds = birds.mapValues mapBirds@{ birdState ->
            val state = birdState.value
            if (state !is BirdState.Alive) return@mapBirds state
            val bird = state.bird
            val spikes = when {
                bird.pos.x < 0 && bird.vel.x < 0 -> leftSpikes
                bird.pos.x > 0 && bird.vel.x > 0 -> rightSpikes
                else -> return@mapBirds state
            }
            val died = (1f - bird.pos.y) < DIE_FROM_SPIKE_DISTANCE ||
                    (bird.pos.y - (-1f) < DIE_FROM_SPIKE_DISTANCE_SQUARED) ||
                    spikes.any { (it - bird.pos).len2 <= DIE_FROM_SPIKE_DISTANCE_SQUARED }
            if (died) BirdState.Dead(currMillis + RECREATE_AFTER_MILLIS) else BirdState.Alive(bird)
        }
    )

    private fun dropItems(): BirdGameState {
        if (candies.size >= MAX_ITEMS_COUNT) return this
        if ((1..ITEM_DROP_EVERY).random() != 1) return this

        fun Float.randomFromSymmetricRange(): Float =
            (-this + DIE_FROM_SPIKE_DISTANCE..this - DIE_FROM_SPIKE_DISTANCE).random()

        val candy = V2(MAX_POS_X.randomFromSymmetricRange(), MAX_POS_Y.randomFromSymmetricRange())
        return copy(candies = candies + candy)
    }

    private fun changeSpikes(): BirdGameState {
        val leftSpikes =
            if (changeLeftSpikes) generateSpikes(-MAX_POS_X) else leftSpikes
        val rightSpikes =
            if (changeRightSpikes) generateSpikes(MAX_POS_X) else rightSpikes
        return copy(
            leftSpikes = leftSpikes,
            rightSpikes = rightSpikes,
            changeLeftSpikes = false,
            changeRightSpikes = false,
        )
    }

    private fun collectItems(): BirdGameState {
        val pointsCopy = HashMap(points)
        var currCandies = candies
        for ((username, birdState) in birds) {
            val bird = birdState.takeAlive()?.pos ?: continue
            val before = currCandies.size
            currCandies = currCandies.filterTo(ArrayList()) { (it - bird).len2 >= COLLECT_CANDY_DISTANCE_SQUARED }
            pointsCopy[username] = (pointsCopy[username] ?: 0) + (before - currCandies.size)
        }
        return copy(candies = currCandies, points = pointsCopy)
    }

    companion object {
        fun empty(): BirdGameState = BirdGameState(
            birds = emptyMap(),
            points = emptyMap(),
            users = emptyMap(),
            candies = emptyList(),
            leftSpikes = emptyList(),
            rightSpikes = emptyList(),
            prevMillis = null
        )
    }
}

private fun generateSpikes(x: Float, maxCount: Int = MAX_SPIKES_COUNT): List<V2> {
    val count = (1..maxCount).random()
    val segment = 2f / count
    return List(count) { idx -> V2(x, y = -1f + segment * idx + nextFloat() * segment) }
}

private enum class BirdDirection {
    Left, Right
}

private val Bird.direction: BirdDirection
    get() = if (vel.x > 0) BirdDirection.Right else BirdDirection.Left

@Suppress("NOTHING_TO_INLINE")
private inline fun BirdState.takeAlive(): Bird? = if (this is BirdState.Alive) bird else null

private inline fun BirdState.mapAlive(currMillis: Long, f: (Bird) -> Bird): BirdState = when {
    this is BirdState.Alive -> f(bird).alive()
    this is BirdState.Dead && currMillis >= recreateAt -> Bird.create().alive()
    else -> this
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Bird.alive(): BirdState = BirdState.Alive(this)

sealed class BirdState {
    data class Dead(val recreateAt: Long) : BirdState()
    data class Alive(val bird: Bird) : BirdState()
}

