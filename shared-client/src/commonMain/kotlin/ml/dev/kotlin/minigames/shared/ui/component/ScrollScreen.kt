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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.essenty.backpressed.BackPressedHandler
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.util.zip
import kotlin.math.roundToInt

internal class ScrollScreenSection private constructor(
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val iconCount: MutableState<Int>,
    val onSelected: () -> Unit,
    val screen: @Composable (BoxScope.() -> Unit)
) {
    companion object {
        fun section(
            icon: ImageVector,
            iconSelected: ImageVector,
            iconCount: MutableState<Int> = mutableStateOf(0),
            onSelected: () -> Unit = {},
            screen: @Composable BoxScope.() -> Unit
        ): ScrollScreenSection = ScrollScreenSection(icon, iconSelected, iconCount, onSelected, screen)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ScrollScreen(
    selectedScreen: MutableState<SelectedScreen>,
    swipeState: SwipeableState<ScreenLocation>,
    up: @Composable BoxScope.() -> Unit,
    left: ScrollScreenSection,
    center: ScrollScreenSection,
    right: ScrollScreenSection,
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
        val scope = rememberCoroutineScope()
        val sections = remember { listOf(left, center, right) }
        val iconSection = when (selectedScreen.value) {
            SelectedScreen.LEFT -> listOf(left.iconSelected, center.icon, right.icon)
            SelectedScreen.CENTER -> listOf(left.icon, center.iconSelected, right.icon)
            SelectedScreen.RIGHT -> listOf(left.icon, center.icon, right.iconSelected)
        }
            .let { icons -> SelectedScreen.values().zip(icons, sections) }

        val scrollOffset by animateDpAsState(targetValue = -fullWidth * selectedScreen.value.ordinal)
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
                            iconSection.forEach { (screen, icon, section) ->
                                val iconCount by section.iconCount
                                BottomIcon(icon, iconPadding, scrollIconSize, iconCount, onClick = {
                                    selectedScreen.value = screen
                                    if (swipeState.targetValue == ScreenLocation.UP) scope.launch {
                                        swipeState.animateTo(ScreenLocation.DOWN)
                                    }
                                    section.onSelected()
                                })
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .offset(scrollOffset)
                            .wrapContentWidth(unbounded = true, align = Alignment.Start)
                    ) {
                        sections.forEach {
                            Box(
                                modifier = Modifier
                                    .width(fullWidth)
                                    .height(height),
                                content = it.screen
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
    badgeCount: Int = 0,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) Badge(
                    modifier = Modifier.offset(
                        x = (-20).dp,
                        y = 44.dp,
                    ),
                    backgroundColor = Color.Red,
                    contentColor = Color.White,
                ) {
                    val badgeText = if (badgeCount > 999) "999+" else "$badgeCount"
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            },
        ) {
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
}

enum class SelectedScreen { LEFT, CENTER, RIGHT }

enum class ScreenLocation { UP, DOWN }
