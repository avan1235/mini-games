package ml.dev.kotlin.minigames.shared.ui.component.bird

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.BirdGameSnapshot
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.ui.ScreenRoute
import ml.dev.kotlin.minigames.shared.ui.component.GameTopBar
import ml.dev.kotlin.minigames.shared.ui.component.bird.BirdNozzleDirection.Companion.fromVelocity
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.util.DpSize
import ml.dev.kotlin.minigames.shared.ui.util.Navigator
import ml.dev.kotlin.minigames.shared.util.V2
import ml.dev.kotlin.minigames.shared.viewmodel.BirdGameViewModel

@Composable
fun BirdGamePlay(
    navigator: Navigator<ScreenRoute>,
    vm: BirdGameViewModel,
    gameState: BirdGameSnapshot,
    clientMessages: MutableStateFlow<GameClientMessage?>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { scope.launch { vm.emitFly(clientMessages) } }
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
                    .padding(
                        top = 80.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray, Shapes.medium)
                ) {
                    val mapSize = DpSize(maxWidth, maxHeight)
                    var last by remember { mutableStateOf(Pair(V2.ZERO, BirdNozzleDirection.Right)) }

                    UpDownConstantSpikes(mapSize)
                    LeftRightSpikes(gameState.spikes, mapSize)
                    gameState.candies.forEach { Candy(it, mapSize) }

                    var foundUser = false
                    for ((username, bird) in gameState.birds) {
                        val direction = fromVelocity(bird.vel)
                        if (vm.username == username) {
                            last = Pair(bird.pos, direction)
                            foundUser = true
                        }
                        Bird(bird.pos, direction, mapSize)
                    }
                    if (!foundUser) Bird(last.first, last.second, mapSize)
                }
            }
            GameTopBar(
                points = vm.points(gameState),
                role = vm.userRole(gameState),
                onClose = { navigator.navigate(ScreenRoute.LogInScreen, dropAll = true) }
            )
        }
    }
}
