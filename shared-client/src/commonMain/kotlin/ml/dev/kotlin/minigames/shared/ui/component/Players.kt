package ml.dev.kotlin.minigames.shared.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableSharedFlow
import ml.dev.kotlin.minigames.shared.component.GameComponent
import ml.dev.kotlin.minigames.shared.model.GameDataClientMessage
import ml.dev.kotlin.minigames.shared.model.GameSnapshot
import ml.dev.kotlin.minigames.shared.model.UserData
import ml.dev.kotlin.minigames.shared.model.Username

@Composable
internal fun <Snapshot : GameSnapshot> Players(
    component: GameComponent<Snapshot>,
    snapshot: Snapshot,
    clientMessages: MutableSharedFlow<GameDataClientMessage>,
) {
    val listState = rememberLazyListState()
    val users = snapshot.users.entries
        .sortedByDescending { component.points(it.key, snapshot) }
        .map { IndexedUserData(it.key, it.value) }.let {
            if (component.username in snapshot.users) it
            else it + IndexedUserData(component.username, UserData.player())
        }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        items(items = users, key = { it.username }) { data ->
            UserDataRow(
                username = data.username,
                userData = data.userData,
                userPoints = component.points(data.username, snapshot),
                canEdit = component.canEditUser(data.username, snapshot),
                onApprove = { component.approve(data.username, clientMessages) },
                onDiscard = { component.discard(data.username, clientMessages) },
            )
        }
    }
}

private data class IndexedUserData(val username: Username, val userData: UserData)
