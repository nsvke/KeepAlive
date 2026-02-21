package com.ec.keepalive.ui.components.contacts_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ec.keepalive.data.local.Contact
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ec.keepalive.ui.components.cards.ContactOne
import kotlinx.coroutines.delay


@Composable
fun ContactList(
    viewModel: ContactListViewModel = viewModel(),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onEditClick: (Contact) -> Unit,
) {
    val listState = rememberLazyListState()

    val contactsState by viewModel.contactListState.collectAsState()

    val contacts = contactsState ?: emptyList()

    LaunchedEffect(contacts, viewModel.recentlyAddedId) {
        val newId = viewModel.recentlyAddedId
        if(newId != null){
            val index = contacts.indexOfFirst { it.id == newId }
            if(index>= 0) {
                listState.animateScrollToItem(index)
                delay(1000)
                viewModel.clearHighlight()
            }
        }
    }

    LazyColumn(state = listState, modifier = modifier, contentPadding = contentPadding) {
        items(
            items = contacts,
            key =  { contact -> contact.id}
        ) { contact ->
            val isHighlighted = contact.id == viewModel.recentlyAddedId

            ContactItemWrapper(
                contact = contact,
                isHighlighted = isHighlighted,
                onDelete = {viewModel.deleteContact(contact)},
                onManuelSeen = { viewModel.updateLastSeenToNow(contact) } ,
                onEditClick = { onEditClick(contact) }
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun LazyItemScope.ContactItemWrapper(
    contact: Contact,
    isHighlighted: Boolean,
    onDelete:() -> Unit,
    onManuelSeen: () -> Unit,
    onEditClick: () -> Unit,
) {
    var isVisible by remember{ mutableStateOf(true) }

    val animationDuration = 600

    LaunchedEffect(isVisible) {
        if(!isVisible) {
            delay(animationDuration.toLong())
            onDelete()
        }
    }

    val highlightColor by animateColorAsState(
        targetValue = if(isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(durationMillis = 1000),
        label = "HighlightAnim"
    )

    Box(
        modifier = Modifier.animateItem(
            placementSpec = tween(durationMillis = animationDuration)
        ).background(highlightColor, RoundedCornerShape(16.dp))
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it / 2 },
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = animationDuration)
            ) + fadeOut(animationSpec = tween(durationMillis = animationDuration)) +
                    shrinkVertically(
                        animationSpec = tween(durationMillis = animationDuration),
                        shrinkTowards = Alignment.Top)
        ) {
            ContactOne(
                name = contact.name,
                number = contact.phoneNumber,
                period = contact.periodAsDays,
                lastseen = contact.lastSeenDate,
                onDelete = { isVisible = false },
                onManuelSeen = onManuelSeen,
                onEdit = onEditClick,
                onPause = {}
            )
        }
    }
}