package com.zoti321.c2cmarket.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    data object Loading : ChatUiState

    data class Ready(
        val productTitle: String,
        val subtitle: String,
        val messages: List<Message>,
        val currentUserId: String,
    ) : ChatUiState

    data class Error(val throwable: Throwable) : ChatUiState
}

sealed interface ChatEvent {
    data object SendFailed : ChatEvent
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val conversationId: Long = checkNotNull(savedStateHandle[Routes.CONVERSATION_ID_ARG])

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val events = _events.asSharedFlow()

    val currentUserId: StateFlow<String> = authRepository.currentUserId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    init {
        viewModelScope.launch {
            chatRepository.markConversationRead(conversationId)
        }
        viewModelScope.launch {
            combine(
                chatRepository.observeConversationForCurrentUser(conversationId),
                chatRepository.observeMessages(conversationId),
                authRepository.currentUserId(),
            ) { conversation, messages, userId ->
                Triple(conversation, messages, userId)
            }.collect { (conversation, messages, userId) ->
                if (conversation == null) {
                    _uiState.value = ChatUiState.Error(IllegalStateException("Conversation not found"))
                } else {
                    val subtitle = when (userId) {
                        conversation.sellerId -> conversation.buyerDisplayName
                        else -> conversation.sellerDisplayName
                    }
                    _uiState.value = ChatUiState.Ready(
                        productTitle = conversation.productTitle,
                        subtitle = subtitle,
                        messages = messages,
                        currentUserId = userId,
                    )
                }
            }
        }
        viewModelScope.launch {
            val savedDraft = chatRepository.observeDraft(conversationId).first()
            if (savedDraft.isNotEmpty()) {
                _inputText.value = savedDraft
            }
            _inputText
                .debounce(300)
                .distinctUntilChanged()
                .collect { text ->
                    chatRepository.saveDraft(conversationId, text)
                }
        }
    }

    fun onInputChanged(text: String) {
        if (text.length <= MAX_INPUT_LENGTH) {
            _inputText.value = text
        }
    }

    fun sendMessage() {
        val text = _inputText.value
        if (text.isBlank() || _isSending.value) return
        viewModelScope.launch {
            _isSending.value = true
            val result = chatRepository.sendMessage(conversationId, text)
            _isSending.value = false
            if (result.isSuccess) {
                _inputText.value = ""
            } else {
                _events.emit(ChatEvent.SendFailed)
            }
        }
    }

    companion object {
        const val MAX_INPUT_LENGTH = 500
    }
}
