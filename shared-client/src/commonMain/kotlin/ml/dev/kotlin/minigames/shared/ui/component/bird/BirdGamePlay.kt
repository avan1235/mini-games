package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.component.BirdComponent
import ml.dev.kotlin.minigames.shared.model.BirdGameSnapshot
import ml.dev.kotlin.minigames.shared.model.GameStateUpdateClientMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.component.ProportionKeeper
import ml.dev.kotlin.minigames.shared.ui.component.bird.BirdNozzleDirection.Companion.fromVelocity
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.V2

@Composable
internal fun BirdGamePlay(
    component: BirdComponent,
    gameState: BirdGameSnapshot,
    stateMessages: MutableStateFlow<GameStateUpdateClientMessage?>,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { component.emitFly(stateMessages) },
        verticalArrangement = Arrangement.Top,
    ) {
        GameTopBar(
            points = component.points(gameState),
            role = component.userRole(gameState),
            onClose = component::closeGame,
        )
        ProportionKeeper {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Gray, Shapes.medium)
            ) {
                val mapSize = DpSize(maxWidth, maxHeight)
                var last by remember { mutableStateOf(Pair(V2.ZERO, BirdNozzleDirection.Right)) }

                UpDownConstantSpikes(mapSize)
                LeftRightSpikes(gameState.spikes, mapSize)
                gameState.candies.forEach { Candy(it, mapSize) }

                var userAlive = false
                for ((username, bird) in gameState.birds) {
                    val direction = fromVelocity(bird.vel)
                    if (component.username == username) {
                        last = Pair(bird.pos, direction)
                        userAlive = true
                    }
                    Bird(bird.pos, direction, mapSize, isAlive = true, theme = CACHED_THEMES[username])
                }
                if (!userAlive) {
                    Bird(
                        last.first,
                        last.second,
                        mapSize,
                        isAlive = false,
                        theme = CACHED_THEMES[component.username]
                    )
                }
            }
        }
    }
}

private val POSSIBLE_THEMES: Set<BirdTheme> = setOf(*BirdTheme.values())

private val CACHED_THEMES: ComputedMap<Username, BirdTheme> = ComputedMap {
    val unused = POSSIBLE_THEMES - values
    val selectFrom = unused.ifEmpty { POSSIBLE_THEMES }
    selectFrom.random()
}
