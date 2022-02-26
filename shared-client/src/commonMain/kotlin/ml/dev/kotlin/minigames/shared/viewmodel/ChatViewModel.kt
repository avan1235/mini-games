package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.SendMessageClientMessage
import ml.dev.kotlin.minigames.shared.model.UserMessage
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.util.now
import java.util.Comparator.comparing

class ChatViewModel(context: ViewModelContext, val username: Username) : ViewModel(context) {

  private val _messages: MutableList<UserMessage> = mutableStateListOf()
  val messages: List<UserMessage> get() = _messages

  val userMessageTextState: MutableState<String> = mutableStateOf("")
  var userMessageText by userMessageTextState

  fun addMessage(message: UserMessage) {
    val idx = _messages.binarySearch(message, comparing(UserMessage::timestamp))
    if (idx < 0) _messages.add(-idx - 1, message)
  }

  suspend fun send(clientMessages: MutableStateFlow<GameClientMessage?>) {
    if (userMessageText.isBlank()) return
    val timestamp = now()
    val userMessage = UserMessage(userMessageText, username, timestamp)
    val message = SendMessageClientMessage(userMessage, timestamp)
    clientMessages.emit(message)
    userMessageText = ""
  }
}
