package ml.dev.kotlin.minigames.shared.component

import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.GameDataClientMessage
import ml.dev.kotlin.minigames.shared.model.SendMessageClientMessage
import ml.dev.kotlin.minigames.shared.model.UserMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.util.now

interface ChatComponent : CountingComponent {
    val username: Username
    val messages: Value<List<UserMessage>>

    val userMessageText: Value<TextFieldValue>
    fun onUserMessageTextChange(text: TextFieldValue)

    fun addMessage(message: UserMessage)

    fun send(clientMessages: MutableSharedFlow<GameDataClientMessage>)
}

class ChatComponentImpl(
    appContext: MiniGamesAppComponentContext,
    componentContext: ComponentContext,
    countPredicate: () -> Boolean,
    override val username: Username,
) : AbstractCountingComponent(appContext, componentContext, countPredicate), ChatComponent {
    private val _messages: MutableStateFlow<List<UserMessage>> = MutableStateFlow(emptyList())
    override val messages: Value<List<UserMessage>> = _messages.asValue()

    private val _userMessageText: MutableStateFlow<TextFieldValue> = MutableStateFlow(TextFieldValue())
    override val userMessageText: Value<TextFieldValue> = _userMessageText.asValue()

    override fun onUserMessageTextChange(text: TextFieldValue) {
        Napier.d { "onUserMessageTextChange: ${text.text}" }
        _userMessageText.value = text
    }

    override fun addMessage(message: UserMessage) {
        countNew()
        _messages.update {
            val idx = it.binarySearch(message, USER_MESSAGES_COMPARATOR)
            if (idx < 0) it.toMutableList().apply { add(-idx - 1, message) } else it
        }

    }

    override fun send(clientMessages: MutableSharedFlow<GameDataClientMessage>) {
        val userMessageText = _userMessageText.value
        if (userMessageText.text.isBlank()) return
        val timestamp = now()
        val userMessage = UserMessage(userMessageText.text, username, timestamp)
        val message = SendMessageClientMessage(userMessage, timestamp)
        scope.launch {
            clientMessages.emit(message)
            _userMessageText.value = TextFieldValue()
        }
    }
}

private val USER_MESSAGES_COMPARATOR: Comparator<UserMessage> =
    Comparator { a, b -> a.timestamp.compareTo(b.timestamp) }
