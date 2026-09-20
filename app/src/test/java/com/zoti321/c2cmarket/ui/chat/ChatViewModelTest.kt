package com.zoti321.c2cmarket.ui.chat

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.zoti321.c2cmarket.data.repository.FakeChatRepository
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.ui.navigation.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val conversationId = 42L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsMessagesIntoReadyState() = runTest(dispatcher) {
        val conversation = sampleConversation()
        val messages = listOf(sampleMessage(body = "hi"))
        val repository = FakeChatRepository(
            conversations = listOf(conversation),
            messages = mapOf(conversationId to messages),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ChatUiState.Ready)
        state as ChatUiState.Ready
        assertEquals("Test Product", state.productTitle)
        assertEquals("卖家", state.sellerDisplayName)
        assertEquals(1, state.messages.size)
    }

    @Test
    fun init_marksConversationRead() = runTest(dispatcher) {
        val repository = FakeChatRepository(conversations = listOf(sampleConversation()))
        createViewModel(repository)

        advanceUntilIdle()

        assertEquals(listOf(conversationId), repository.markReadCalls)
    }

    @Test
    fun sendMessage_success_clearsInput() = runTest(dispatcher) {
        val repository = FakeChatRepository(conversations = listOf(sampleConversation()))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onInputChanged("hello")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals("", viewModel.inputText.value)
        assertEquals(listOf(conversationId to "hello"), repository.sendCalls)
    }

    @Test
    fun sendMessage_failure_emitsSendFailedEvent() = runTest(dispatcher) {
        val repository = FakeChatRepository(conversations = listOf(sampleConversation())).apply {
            sendResult = Result.failure(IllegalStateException("network"))
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onInputChanged("hello")
        viewModel.events.test {
            viewModel.sendMessage()
            advanceUntilIdle()
            assertEquals(ChatEvent.SendFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("hello", viewModel.inputText.value)
    }

    private fun createViewModel(repository: FakeChatRepository): ChatViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(Routes.CONVERSATION_ID_ARG to conversationId))
        return ChatViewModel(savedStateHandle, repository)
    }

    private fun sampleConversation() = Conversation(
        id = conversationId,
        productId = 1,
        productTitle = "Test Product",
        productImageUrl = "https://example.com/img.jpg",
        sellerId = "seller-1",
        sellerDisplayName = "卖家",
        buyerId = GuestSession.GUEST_ID,
        lastMessagePreview = "",
        lastMessageAt = 100L,
        unreadCount = 0,
        createdAt = 100L,
    )

    private fun sampleMessage(body: String) = Message(
        id = 1L,
        conversationId = conversationId,
        senderId = GuestSession.GUEST_ID,
        body = body,
        status = MessageStatus.SENT,
        sentAt = 100L,
        isRead = true,
    )
}
