package ml.dev.kotlin.minigames.shared.model

import kotlinx.serialization.Serializable
import ml.dev.kotlin.minigames.shared.util.everyUnorderedTriple
import kotlin.math.max

const val SELECT_CARDS: Int = 3
const val TABLE_CARDS: Int = 12

@Serializable
enum class CardFill { Full, Part, None }

@Serializable
enum class CardColor { Green, Purple, Pink }

@Serializable
enum class CardShape { Diamond, Oval, Squiggle }

@Serializable
enum class CardCount { One, Two, Three }

@Serializable
data class SetCard(
    val count: CardCount,
    val shape: CardShape,
    val color: CardColor,
    val fill: CardFill,
) {
    companion object {
        fun all(): Set<SetCard> = HashSet<SetCard>().apply {
            for (cnt in CardCount.values()) for (shape in CardShape.values())
                for (col in CardColor.values()) for (fill in CardFill.values()) add(
                    SetCard(cnt, shape, col, fill)
                )
        }
    }
}

@Serializable
data class TableCards(
    val cardsById: Map<Int, SetCard>,
)

@Serializable
data class DeckCards(
    val cards: Set<SetCard>,
)

@Serializable
data class SetProposal(
    val cardsIds: Set<Int>,
)

@Serializable
data class SetGameSnapshot(
    val table: TableCards,
    override val points: Map<Username, Int>,
    override val users: Map<Username, UserData>,
) : GameSnapshot

@Serializable
data class SetGameUpdate(
    private val proposal: SetProposal,
) : GameUpdate {

    override fun update(forUser: Username, gameState: GameState, currMillis: Long): GameState = when {
        gameState !is SetGameState -> gameState
        else -> run {
            val isValidSetProposal = gameState.table.cardsById.isValidSetProposal(proposal.cardsIds)
            val pointsChange = if (isValidSetProposal) 1 else -1
            val pointsUpdate = forUser to max(0, (gameState.points[forUser] ?: 0) + pointsChange)
            if (!isValidSetProposal) {
                return@run gameState.copy(points = gameState.points + pointsUpdate)
            }
            val (tableUpdate, deckUpdate) = updateDeckTable(proposal, gameState.table, gameState.deck)
            when (gameState.table) {
                tableUpdate -> SetGameState.random(
                    points = gameState.points + pointsUpdate,
                    users = gameState.users,
                )

                else -> gameState.copy(
                    table = tableUpdate,
                    deck = deckUpdate,
                    points = gameState.points + pointsUpdate
                )
            }
        }
    }
}

data class SetGameState(
    val table: TableCards,
    val deck: DeckCards,
    override val points: Map<Username, Int>,
    override val users: Map<Username, UserData>,
) : GameState() {

    override fun updateWith(users: Map<Username, UserData>, points: Map<Username, Int>): SetGameState =
        copy(users = users, points = points)

    override fun snapshot(forUser: Username): SetGameSnapshot =
        SetGameSnapshot(table, points, users)

    override fun snapshot(): CumulativeGameSnapshot =
        CumulativeGameSnapshot.SameForAllUsers(SetGameSnapshot(table, points, users))

    companion object {
        fun random(
            points: Map<Username, Int> = emptyMap(),
            users: Map<Username, UserData> = emptyMap(),
        ): SetGameState {
            val cards = SetCard.all().shuffled()
            val table = TableCards(cards.take(TABLE_CARDS).withIndex().associate { it.index to it.value })
            val deck = DeckCards(cards.drop(TABLE_CARDS).toHashSet())
            return SetGameState(table, deck, points, users)
        }
    }
}

private fun Map<Int, SetCard>.validProposal(): Set<Int>? {
    keys.everyUnorderedTriple { fst, snd, trd ->
        val proposal = setOf(fst, snd, trd)
        if (isValidSetProposal(proposal)) return proposal
    }
    return null
}

private fun Map<Int, SetCard>.isValidSetProposal(cardsIds: Set<Int>): Boolean {
    val cards = cardsIds.mapNotNull { this[it] }
    if (cards.size != SELECT_CARDS) return false

    fun validSetProperty(selector: (SetCard) -> Any): Boolean {
        val v = cards.map(selector)
        return (v[0] == v[1] && v[1] == v[2]) || (v[0] != v[1] && v[1] != v[2] && v[2] != v[0])
    }

    if (!validSetProperty { it.color }) return false
    if (!validSetProperty { it.count }) return false
    if (!validSetProperty { it.fill }) return false
    if (!validSetProperty { it.shape }) return false

    return true
}

private operator fun DeckCards.minus(cards: List<SetCard>): DeckCards =
    copy(cards = this.cards - cards.toSet())

private data class TableDeckUpdate(val table: TableCards, val deck: DeckCards)

private fun updateDeckTable(proposal: SetProposal, table: TableCards, deck: DeckCards): TableDeckUpdate {
    val updatedCards = HashMap(table.cardsById)
    proposal.cardsIds.forEach { updatedCards -= it }

    deck.cards.everyUnorderedTriple { fst, snd, trd ->
        val replacementCards = listOf(fst, snd, trd)
        val currUpdated = HashMap(updatedCards)
        proposal.cardsIds.zip(replacementCards).forEach { (id, card) -> currUpdated[id] = card }
        if (currUpdated.validProposal() != null) return TableDeckUpdate(
            table = TableCards(currUpdated),
            deck = deck - replacementCards
        )
    }
    return TableDeckUpdate(TableCards(updatedCards), deck)
}
