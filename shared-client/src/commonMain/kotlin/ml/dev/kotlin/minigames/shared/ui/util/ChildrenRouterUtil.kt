package ml.dev.kotlin.minigames.shared.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value

typealias ValueObserver<T> = (T) -> Unit

typealias ChildContent<C, T> = @Composable (child: Child.Created<C, T>) -> Unit

typealias ChildAnimation<C, T> = @Composable (RouterState<C, T>, Modifier, ChildContent<C, T>) -> Unit

typealias ChildAnimator<C, T> =
        @Composable (
                child: Child.Created<C, T>,
                factor: Float,
                placement: ChildPlacement,
                direction: ChildAnimationDirection,
                content: @Composable () -> Unit
        ) -> Unit

@Composable
internal fun <C : Any, T : Any> Children(
        routerState: Value<RouterState<C, T>>,
        modifier: Modifier = Modifier,
        animation: ChildAnimation<C, T>,
        content: ChildContent<C, T>
) {
    val state = routerState.subscribeAsState()

    val holder = rememberSaveableStateHolder()

    holder.retainStates(state.value.getConfigurations())

    animation(state.value, modifier) { child ->
        holder.SaveableStateProvider(child.configuration.key()) {
            content(child)
        }
    }
}

private fun RouterState<*, *>.getConfigurations(): Set<String> {
    val set = HashSet<String>()
    backStack.forEach { set += it.configuration.key() }
    set += activeChild.configuration.key()

    return set
}

@Composable
private fun <T : Any> Value<T>.subscribeAsState(): State<T> {
    val state = remember(this) { mutableStateOf(value) }

    DisposableEffect(this) {
        val observer: ValueObserver<T> = { state.value = it }
        subscribe(observer)

        onDispose {
            unsubscribe(observer)
        }
    }
    return state
}

private fun Any.key(): String = "${this::class.simpleName}_${hashCode().toString(radix = 36)}"

@Composable
private fun SaveableStateHolder.retainStates(currentKeys: Set<Any>) {
    val keys = remember(this) { Keys(currentKeys) }

    DisposableEffect(this, currentKeys) {
        keys.set.forEach {
            if (it !in currentKeys) {
                removeState(it)
            }
        }

        keys.set = currentKeys

        onDispose {}
    }
}

private class Keys(
        var set: Set<Any>
)

enum class ChildPlacement {
    BACK, FRONT,
}

enum class ChildAnimationDirection {
    EXIT, ENTER
}

fun <C : Any, T : Any> crossfadeScale(
        animationSpec: FiniteAnimationSpec<Float> = tween(),
        enterScaleFactor: Float = 1.15F,
        exitScaleFactor: Float = 0.95F
): ChildAnimation<C, T> =
        childAnimation(animationSpec = animationSpec) { _, factor, placement, _, content ->
            Box(
                    modifier = Modifier
                            .scale(
                                    when (placement) {
                                        ChildPlacement.BACK -> exitScaleFactor + (1F - exitScaleFactor) * factor
                                        ChildPlacement.FRONT -> enterScaleFactor - (enterScaleFactor - 1F) * factor
                                    }
                            )
                            .alpha(factor)
            ) {
                content()
            }
        }

private fun <C : Any, T : Any> childAnimation(
        animationSpec: FiniteAnimationSpec<Float> = tween(),
        animator: ChildAnimator<C, T>
): ChildAnimation<C, T> =
        { routerState, modifier, content ->
            ChildAnimationImpl(
                    targetPage = Page(routerState.activeChild, routerState.backStack.size),
                    modifier = modifier,
                    animationSpec = animationSpec,
                    animator = animator,
                    content = content
            )
        }

@Composable
private fun <C : Any, T : Any> ChildAnimationImpl(
        targetPage: Page<C, T>,
        modifier: Modifier,
        animationSpec: FiniteAnimationSpec<Float>,
        animator: ChildAnimator<C, T>,
        content: ChildContent<C, T>,
) {
    var pages: Pages<C, T> by remember { mutableStateOf(Pages(target = targetPage)) }
    if (targetPage.configuration != pages.target.configuration) {
        pages = Pages(target = targetPage, previous = pages.target)
    }

    val new = pages.target
    val old = pages.previous

    val animationState = remember(new.configuration) { AnimationState(if (old == null) 1F else 0F) }

    LaunchedEffect(new.configuration) {
        animationState.animateTo(
                targetValue = 1F,
                animationSpec = animationSpec,
                sequentialAnimation = !animationState.isFinished
        )

        pages = Pages(target = pages.target)
    }

    val items = rememberAnimationItems(targetPage = new, previousPage = old)

    Box(modifier = modifier) {
        items.forEach { item ->
            key(item.page.child.configuration) {
                animator(
                        item.page.child,
                        when (item.direction) {
                            ChildAnimationDirection.ENTER -> animationState.value
                            ChildAnimationDirection.EXIT -> 1F - animationState.value
                        },
                        item.placement,
                        item.direction
                ) {
                    content(item.page.child)
                }
            }
        }
    }
}

@Composable
private fun <C : Any, T : Any> rememberAnimationItems(
        targetPage: Page<C, T>,
        previousPage: Page<C, T>?
): List<AnimationItem<C, T>> =
        remember(targetPage.configuration, previousPage?.configuration) {
            when {
                previousPage == null ->
                    listOf(AnimationItem(targetPage, ChildPlacement.BACK, ChildAnimationDirection.ENTER))

                targetPage.index >= previousPage.index ->
                    listOf(
                            AnimationItem(previousPage, ChildPlacement.BACK, ChildAnimationDirection.EXIT),
                            AnimationItem(targetPage, ChildPlacement.FRONT, ChildAnimationDirection.ENTER),
                    )

                else ->
                    listOf(
                            AnimationItem(targetPage, ChildPlacement.BACK, ChildAnimationDirection.ENTER),
                            AnimationItem(previousPage, ChildPlacement.FRONT, ChildAnimationDirection.EXIT),
                    )
            }
        }

private class Page<out C : Any, out T : Any>(
        val child: Child.Created<C, T>,
        val index: Int,
) {
    val configuration: C = child.configuration
}

private class Pages<out C : Any, out T : Any>(
        val target: Page<C, T>,
        val previous: Page<C, T>? = null,
)

private class AnimationItem<out C : Any, out T : Any>(
        val page: Page<C, T>,
        val placement: ChildPlacement,
        val direction: ChildAnimationDirection,
)
