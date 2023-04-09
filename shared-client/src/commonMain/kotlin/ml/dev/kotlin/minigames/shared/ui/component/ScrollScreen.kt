package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.backpressed.BackPressedHandler
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ScrollScreen(
        up: @Composable BoxScope.() -> Unit,
        leftScreen: @Composable BoxScope.() -> Unit,
        centerScreen: @Composable BoxScope.() -> Unit,
        rightScreen: @Composable BoxScope.() -> Unit,
        leftIcon: ImageVector,
        leftIconSelected: ImageVector,
        centerIcon: ImageVector,
        centerIconSelected: ImageVector,
        rightIcon: ImageVector,
        rightIconSelected: ImageVector,
        backPressedHandler: BackPressedHandler,
        onUp: () -> Unit,
        onDown: () -> Unit,
        threshold: Float = 0.3f,
        scrollIconSize: Dp = 32.dp,
        iconPadding: Dp = 16.dp,
): Unit = with(LocalDensity.current) {
    BoxWithConstraints {
        val fullHeight = maxHeight
        val fullWidth = maxWidth
        val height = fullHeight - scrollIconSize - (iconPadding * 2)
        val swipeState = rememberSwipeableState(ScreenLocation.UP)
        val scope = rememberCoroutineScope()
        var selectedScreen by remember { mutableStateOf(SelectedScreen.CENTER) }
        val screens = remember { listOf(leftScreen, centerScreen, rightScreen) }
        val iconScreens = when (selectedScreen) {
            SelectedScreen.LEFT -> listOf(leftIconSelected, centerIcon, rightIcon)
            SelectedScreen.CENTER -> listOf(leftIcon, centerIconSelected, rightIcon)
            SelectedScreen.RIGHT -> listOf(leftIcon, centerIcon, rightIconSelected)
        }.zip(SelectedScreen.values())

        val scrollOffset by animateDpAsState(targetValue = -fullWidth * selectedScreen.ordinal)
        val handler = remember { fun() = true.also { scope.launch { swipeState.animateTo(ScreenLocation.UP) } } }
        when (swipeState.targetValue) {
            ScreenLocation.DOWN -> {
                backPressedHandler.register(handler)
                onDown()
            }

            ScreenLocation.UP -> {
                backPressedHandler.unregister(handler)
                onUp()
            }
        }

        Box(
                modifier = Modifier
                        .fillMaxWidth()
                        .height(fullHeight)
        ) {
            Box(
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .background(MaterialTheme.colors.surface),
                    content = up
            )
            Box(
                    modifier = Modifier
                            .offset { IntOffset(0, swipeState.offset.value.roundToInt()) }
                            .fillMaxWidth()
                            .height(fullHeight)
                            .background(MaterialTheme.colors.surface)
                            .swipeable(
                                    state = swipeState,
                                    anchors = mapOf(0f to ScreenLocation.DOWN, height.toPx() to ScreenLocation.UP),
                                    thresholds = { _, _ -> FractionalThreshold(threshold) },
                                    orientation = Orientation.Vertical
                            )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.shadow(elevation = 2.dp)) {
                        Row(
                                modifier = Modifier
                                        .background(MaterialTheme.colors.background)
                                        .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            iconScreens.forEach { (icon, screen) ->
                                BottomIcon(icon, iconPadding, scrollIconSize, onClick = {
                                    selectedScreen = screen
                                    if (swipeState.targetValue == ScreenLocation.UP) scope.launch {
                                        swipeState.animateTo(ScreenLocation.DOWN)
                                    }
                                })
                            }
                        }
                    }
                    Row(
                            modifier = Modifier
                                    .offset(scrollOffset)
                                    .wrapContentWidth(unbounded = true, align = Alignment.Start)
                    ) {
                        screens.forEach {
                            Box(
                                    modifier = Modifier
                                            .width(fullWidth)
                                            .height(height),
                                    content = it
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BottomIcon(
        icon: ImageVector,
        padding: Dp,
        iconsSize: Dp,
        onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        IconButton(
                onClick = onClick,
                modifier = Modifier.padding(padding)
        ) {
            ShadowIcon(
                    imageVector = icon,
                    contentDescription = "selectIcon",
                    size = iconsSize
            )
        }
    }
}

private enum class SelectedScreen { LEFT, CENTER, RIGHT }

private enum class ScreenLocation { UP, DOWN }
