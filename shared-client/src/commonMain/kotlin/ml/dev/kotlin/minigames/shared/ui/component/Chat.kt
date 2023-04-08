package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.UserMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.ui.util.randomColor
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.format
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.util.toPaddedString
import ml.dev.kotlin.minigames.shared.viewmodel.ChatViewModel

@Composable
internal fun Chat(
    vm: ChatViewModel,
    clientMessages: MutableStateFlow<GameClientMessage?>,
    size: Dp = 56.dp,
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val messagesHeight = maxHeight - size
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
        ) {
            Messages(vm.messages, vm.username, messagesHeight)
            MessageInput(vm.userMessageTextState, size = size, onClick = { scope.launch { vm.send(clientMessages) } })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MessageInput(
    message: MutableState<String>,
    size: Dp,
    padding: Dp = 8.dp,
    inputRegex: Regex = Regex("[^\\n]*"),
    onClick: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(size)) {
        val messageWidth = maxWidth - size
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colors.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = message.value,
                onValueChange = { message.value = if (inputRegex.matches(it)) it else message.value },
                textStyle = Typography.body1.copy(color = MaterialTheme.colors.primary),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                keyboardActions = KeyboardActions(
                    onDone = { onClick() },
                ),
                modifier = Modifier
                    .width(messageWidth)
                    .padding(start = padding, top = padding, bottom = padding)
                    .clip(Shapes.large)
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = padding * 2, vertical = padding)
                    .onKeyEvent {
                        if (it.key.keyCode == Key.Enter.keyCode) true.also { onClick() } else false
                    }
            )
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(size)
                    .padding(padding)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.secondaryVariant)
                    .padding(padding),
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "send")
            }
        }
    }
}

@Composable
private fun Messages(messages: List<UserMessage>, username: Username, height: Dp) {
    val state = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(height),
        state = state,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        itemsIndexed(messages, key = { _, msg -> msg.uuid }) { idx, msg ->
            Message(msg, username, onVisible = { state.animateScrollToItem(idx) })
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Message(
    message: UserMessage,
    username: Username,
    contentPart: Float = 0.9f,
    animationDuration: Int = 300,
    paddingMultiply: Float = 1.5f,
    onVisible: suspend () -> Unit
) {
    val isAuthor = message.author == username
    val background = if (isAuthor) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.primaryVariant
    val align = if (isAuthor) Alignment.CenterEnd else Alignment.CenterStart
    val transformOrigin = if (isAuthor) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)
    val animationSpec = tween<Float>(animationDuration, easing = LinearEasing)
    var visible by remember(message) { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(message.timestamp - now())
        visible = true
        onVisible()
    }
    AnimatedVisibility(
        visible,
        enter = scaleIn(transformOrigin = transformOrigin, animationSpec = animationSpec),
        exit = scaleOut(transformOrigin = transformOrigin, animationSpec = animationSpec)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = align
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(contentPart), contentAlignment = align
            ) {
                Surface(
                    shape = Shapes.large, color = background, elevation = 4.dp, modifier = Modifier.padding(4.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        if (!isAuthor) Text(
                            text = message.author,
                            style = Typography.subtitle2,
                            color = USER_COLOR[message.author],
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            message.message,
                            style = Typography.body1,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(
                                top = if (!isAuthor) with(LocalDensity.current) { Typography.subtitle2.fontSize.toDp() * paddingMultiply } else 0.dp,
                                bottom = with(LocalDensity.current) { Typography.caption.fontSize.toDp() * paddingMultiply },
                            )
                        )
                        Text(
                            text = message.timestamp.format { "${hour.toPaddedString()}:${minute.toPaddedString()}" },
                            style = Typography.caption,
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}

private val USER_COLOR: ComputedMap<Username, Color> = ComputedMap { randomColor() }
