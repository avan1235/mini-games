package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ml.dev.kotlin.minigames.shared.model.GameClientMessage
import ml.dev.kotlin.minigames.shared.model.GameSnapshot
import ml.dev.kotlin.minigames.shared.model.UserData
import ml.dev.kotlin.minigames.shared.model.Username
import ml.dev.kotlin.minigames.shared.ui.screen.LocalToastContext
import ml.dev.kotlin.minigames.shared.viewmodel.GameViewModel

@Composable
fun <Snapshot : GameSnapshot> Players(
  vm: GameViewModel<Snapshot>,
  snapshot: Snapshot,
  clientMessages: MutableStateFlow<GameClientMessage?>
): Unit = with(LocalToastContext.current) {
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val users = snapshot.users.entries
    .map { IndexedUserData(it.key, it.value) }.let {
      if (vm.username in snapshot.users) it
      else it + IndexedUserData(vm.username, UserData.player())
    }

  LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    state = listState
  ) {
    items(items = users, key = { it.username }) { data ->
      UserDataRow(
        username = data.username,
        userData = data.userData,
        userPoints = vm.points(data.username, snapshot),
        canEdit = vm.canEditUser(data.username, snapshot),
        onApprove = {
          toast("Approving ${data.username}")
          scope.launch { vm.approve(data.username, clientMessages) }
        },
        onDiscard = {
          toast("Discarding ${data.username}")
          scope.launch { vm.discard(data.username, clientMessages) }
        },
      )
    }
  }
}

private data class IndexedUserData(val username: Username, val userData: UserData)
