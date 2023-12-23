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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import ml.dev.kotlin.minigames.shared.component.ChatComponent
import ml.dev.kotlin.minigames.shared.model.GameDataClientMessage
import ml.dev.kotlin.minigames.shared.model.UserMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.theme.Shapes
import ml.dev.kotlin.minigames.shared.ui.theme.Typography
import ml.dev.kotlin.minigames.shared.ui.util.randomColor
import ml.dev.kotlin.minigames.shared.util.ComputedMap
import ml.dev.kotlin.minigames.shared.util.format
import ml.dev.kotlin.minigames.shared.util.now
import ml.dev.kotlin.minigames.shared.util.toPaddedString

@Composable
internal fun Chat(
    component: ChatComponent,
    clientMessages: MutableSharedFlow<GameDataClientMessage>,
    size: Dp = 54.dp,
    bottomPadding: Dp = 16.dp,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val messagesHeight = maxHeight - size - bottomPadding
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
        ) {
            val messages by component.messages.subscribeAsState()
            Messages(messages, component.username, messagesHeight)

            val userMessageText by component.userMessageText.subscribeAsState()
            MessageInput(userMessageText, component::onUserMessageTextChange, size, onClick = { component.send(clientMessages) })
            BottomPadding(bottomPadding)
        }
    }
}

@Composable
private fun BottomPadding(padding: Dp) {
    Box(modifier = Modifier.fillMaxWidth().height(padding).background(MaterialTheme.colorScheme.background))
}

@Composable
private fun MessageInput(
    message: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    size: Dp,
    onClick: () -> Unit,
    padding: Dp = 8.dp,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(size)
    ) {
        val messageWidth = maxWidth - size
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = message,
                onValueChange = onMessageChange,
                textStyle = Typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardActions = KeyboardActions(
                    onDone = { onClick() },
                ),
                modifier = Modifier
                    .width(messageWidth)
                    .padding(start = padding, top = padding, bottom = padding)
                    .clip(Shapes.large)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = padding * 2, vertical = padding)
                    .onKeyEvent {
                        if (it.key.keyCode == Key.Enter.keyCode) true.also { onClick() } else false
                    }
            )
            Box(
                modifier = Modifier.fillMaxHeight().padding(padding)
            ) {
                IconButton(onClick) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "send")
                }
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

@Composable
private fun Message(
    message: UserMessage,
    username: Username,
    contentPart: Float = 0.9f,
    animationDuration: Int = 300,
    paddingMultiply: Float = 1.5f,
    onVisible: suspend () -> Unit,
) {
    val isAuthor = message.author == username
    val background = if (isAuthor) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.inversePrimary
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
                    shape = Shapes.large,
                    color = background,
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        if (!isAuthor) Text(
                            text = message.author,
                            style = Typography.titleSmall,
                            color = USER_COLOR[message.author],
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        Text(
                            message.message,
                            style = Typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    start = 0.dp,
                                    end = 0.dp,
                                    top = if (!isAuthor) with(LocalDensity.current) { Typography.titleSmall.fontSize.toDp() * paddingMultiply } else 0.dp,
                                    bottom = with(LocalDensity.current) { Typography.bodySmall.fontSize.toDp() * paddingMultiply },
                                )
                        )
                        Text(
                            text = message.timestamp.format { "${hour.toPaddedString()}:${minute.toPaddedString()}" },
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}

private val USER_COLOR: ComputedMap<Username, Color> = ComputedMap { randomColor() }
