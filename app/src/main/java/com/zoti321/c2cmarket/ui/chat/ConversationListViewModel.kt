package com.zoti321.c2cmarket.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.ConversationRole
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.asUiStateFlow
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    val role: ConversationRole = ConversationRole.fromRouteArg(savedStateHandle[Routes.CONVERSATION_ROLE_ARG])

    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<UiState<List<Conversation>>> = refreshSignal
        .flatMapLatest {
            val flow = when (role) {
                ConversationRole.BUYER -> chatRepository.observeConversationsAsBuyer()
                ConversationRole.SELLER -> chatRepository.observeConversationsAsSeller()
            }
            flow.asUiStateFlow()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        refreshSignal.tryEmit(Unit)
    }
}
