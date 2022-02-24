package ml.dev.kotlin.minigames.shared.viewmodel

import androidx.compose.runtime.mutableStateListOf
import ml.dev.kotlin.minigames.shared.model.UserMessage

class MessagesViewModel(context: ViewModelContext) : ViewModel(context) {

  val messages = mutableStateListOf<UserMessage>()

  fun addMessage(userMessage: UserMessage) {
    messages.add(userMessage)
  }
}
