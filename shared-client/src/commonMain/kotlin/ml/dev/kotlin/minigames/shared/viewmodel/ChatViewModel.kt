package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableSharedFlow
import ml.dev.kotlin.minigames.shared.model.GameDataClientMessage
import ml.dev.kotlin.minigames.shared.model.SendMessageClientMessage
import ml.dev.kotlin.minigames.shared.model.UserMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.component.SelectedScreen
import ml.dev.kotlin.minigames.shared.util.now

internal class ChatViewModel(
    context: ViewModelContext,
    countPredicate: () -> Boolean,
    val username: Username,
) : CountingViewModel<SelectedScreen>(
    ctx = context,
    countPredicate = countPredicate
) {

    private val _messages: MutableList<UserMessage> = mutableStateListOf()
    val messages: List<UserMessage> get() = _messages

    val userMessageTextState: MutableState<String> = mutableStateOf("")
    var userMessageText by userMessageTextState

    fun addMessage(message: UserMessage) {
        countNew()
        val idx = _messages.binarySearch(message, USER_MESSAGES_COMPARATOR)
        if (idx < 0) _messages.add(-idx - 1, message)
    }

    suspend fun send(clientMessages: MutableSharedFlow<GameDataClientMessage>) {
        if (userMessageText.isBlank()) return
        val timestamp = now()
        val userMessage = UserMessage(userMessageText, username, timestamp)
        val message = SendMessageClientMessage(userMessage, timestamp)
        clientMessages.emit(message)
        userMessageText = ""
    }
}

private val USER_MESSAGES_COMPARATOR: Comparator<UserMessage> =
    Comparator { a, b -> a.timestamp.compareTo(b.timestamp) }