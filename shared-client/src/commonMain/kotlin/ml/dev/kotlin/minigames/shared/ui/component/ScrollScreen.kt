package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import `in`.procyk.compose.util.SystemBarsScreen
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.ui.util.ConstantValue
import ml.dev.kotlin.minigames.shared.util.unit
import ml.dev.kotlin.minigames.shared.util.zip
import kotlin.math.roundToInt

internal class ScrollScreenSection private constructor(
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val iconCount: Value<Int>,
    val onSelected: () -> Unit,
    val screen: @Composable (BoxScope.() -> Unit),
) {
    companion object {
        fun section(
            icon: ImageVector,
            iconSelected: ImageVector,
            iconCount: Value<Int> = ConstantValue(0),
            onSelected: () -> Unit = {},
            screen: @Composable BoxScope.() -> Unit,
        ): ScrollScreenSection = ScrollScreenSection(icon, iconSelected, iconCount, onSelected, screen)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScrollScreen(
    selectedScreen: MutableState<SelectedScreen>,
    swipeState: SwipeableState<ScreenLocation>,
    up: @Composable BoxScope.() -> Unit,
    left: ScrollScreenSection,
    center: ScrollScreenSection,
    right: ScrollScreenSection,
    backHandler: BackHandler,
    onUp: () -> Unit,
    onDown: () -> Unit,
    threshold: Float = 0.3f,
    scrollIconSize: Dp = 32.dp,
    iconPadding: Dp = 16.dp,
) {
    with(LocalDensity.current) {
        SystemBarsScreen(
            top = MaterialTheme.colorScheme.surface,
            bottom = MaterialTheme.colorScheme.surface,
        ) {
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
                val handler = remember {
                    object : BackCallback() {
                        override fun onBack() = scope.launch { swipeState.animateTo(ScreenLocation.UP) }.unit()
                    }.also(backHandler::register)
                }
                when (swipeState.targetValue) {
                    ScreenLocation.DOWN -> {
                        handler.isEnabled = true
                        onDown()
                    }

                    ScreenLocation.UP -> {
                        handler.isEnabled = true
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
                            .background(MaterialTheme.colorScheme.surface),
                        content = up
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, swipeState.offset.value.roundToInt()) }
                            .fillMaxWidth()
                            .height(fullHeight)
                            .background(MaterialTheme.colorScheme.surface)
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
                                        .background(MaterialTheme.colorScheme.background)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    iconSection.forEach { (screen, icon, section) ->
                                        val iconCount by section.iconCount.subscribeAsState()
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                AnimatedVisibility(
                    visible = badgeCount > 0,
                    enter = expandIn(
                        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                        expandFrom = Alignment.Center
                    ),
                    exit = shrinkOut(
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                        shrinkTowards = Alignment.Center
                    ),
                ) {
                    Badge(
                        modifier = Modifier.offset(
                            x = (-20).dp,
                            y = 44.dp,
                        ),
                        containerColor = Color.Red,
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
