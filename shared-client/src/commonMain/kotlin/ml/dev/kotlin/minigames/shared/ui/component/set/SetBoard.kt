package ml.dev.kotlin.minigames.shared.ui.component.set

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ml.dev.kotlin.minigames.shared.model.SELECT_CARDS
import ml.dev.kotlin.minigames.shared.model.SetGameSnapshot
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun SetBoard(
  setGame: SetGameSnapshot,
  onProposal: (Set<Int>) -> Unit,
  maxCardRatio: Float = 1.25f,
  columns: Int = 3,
): Unit = with(LocalDensity.current) {
  val cards = setGame.table.cardsById
  val rows = ceil(cards.size.toDouble() / columns.toDouble()).roundToInt()

  var size by remember { mutableStateOf(IntSize.Zero) }
  var setProposal by remember { mutableStateOf(emptySet<Int>()) }
  val selectedCards = remember { cards.keys.associateWith { mutableStateOf(false) } }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { size = it.size },
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      val width = size.width.toDp() / columns
      val height = min(width * maxCardRatio, size.height.toDp() / rows)

      for (rowCards in cards.keys.sorted().windowed(size = columns, step = columns))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (id in rowCards) {
            val card = cards[id] ?: continue
            val selectState = selectedCards[id] ?: continue
            var selected by selectState

            SetCard(card, selected, width, height) {
              when {
                selected -> {
                  setProposal = setProposal - id
                  selected = false
                }
                !selected && setProposal.size < SELECT_CARDS -> {
                  setProposal = setProposal + id
                  selected = true
                  if (setProposal.size == SELECT_CARDS) {
                    onProposal(setProposal)
                    setProposal = emptySet()
                    selectedCards.values.forEach { it.value = false }
                  }
                }
              }
            }
          }
        }
    }

  }
}
