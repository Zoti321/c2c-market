package com.zoti321.c2cmarket.ui.chat

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.zoti321.c2cmarket.ui.fake.ScreenTestViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class ConversationListScreenTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun conversationListScreen_showsEmptyState() {
        composeRule.setContent {
            ConversationListScreen(
                onBack = {},
                onConversationClick = {},
                viewModel = ScreenTestViewModels.conversationList(),
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("暂无消息").assertExists()
    }

    @Test
    fun conversationListScreen_sellerRole_showsSellerEmptyState() {
        composeRule.setContent {
            ConversationListScreen(
                onBack = {},
                onConversationClick = {},
                viewModel = ScreenTestViewModels.sellerConversationList(),
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("暂无买家咨询").assertExists()
    }
}
