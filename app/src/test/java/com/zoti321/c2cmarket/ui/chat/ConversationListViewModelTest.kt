package com.zoti321.c2cmarket.ui.chat

import com.zoti321.c2cmarket.data.repository.FakeChatRepository
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class ConversationListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emptyList_emitsSuccessWithEmptyData() = runTest(dispatcher) {
        val viewModel = ConversationListViewModel(FakeChatRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(emptyList<Conversation>(), (state as UiState.Success).data)
    }

    @Test
    fun uiState_withConversations_emitsSortedList() = runTest(dispatcher) {
        val conversations = listOf(
            sampleConversation(id = 2L, title = "Newer", lastMessageAt = 300L),
            sampleConversation(id = 1L, title = "Older", lastMessageAt = 100L),
        )
        val viewModel = ConversationListViewModel(
            FakeChatRepository(conversations = conversations),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(listOf(2L, 1L), data.map { it.id })
    }

    private fun sampleConversation(
        id: Long,
        title: String,
        lastMessageAt: Long,
    ) = Conversation(
        id = id,
        productId = id.toInt(),
        productTitle = title,
        productImageUrl = "https://example.com/img.jpg",
        sellerId = "seller-$id",
        sellerDisplayName = "卖家",
        buyerId = GuestSession.GUEST_ID,
        lastMessagePreview = "preview",
        lastMessageAt = lastMessageAt,
        unreadCount = 0,
        createdAt = lastMessageAt,
    )
}
