package com.zoti321.c2cmarket.ui.profile

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
class ProfileScreenTest {

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
    fun profileScreen_showsGuestMode() {
        composeRule.setContent {
            ProfileScreen(
                callbacks = ProfileScreenCallbacks(
                    onOrderClick = {},
                    onProductClick = {},
                    onCreateListing = {},
                    onEditListing = {},
                    onManageAddresses = {},
                    onBuyerMessagesClick = {},
                    onSellerMessagesClick = {},
                    onFavoritesClick = {},
                ),
                viewModel = ScreenTestViewModels.guestProfile(),
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("游客模式").assertExists()
    }

    @Test
    fun profileScreen_showsV4Entries() {
        composeRule.setContent {
            ProfileScreen(
                callbacks = ProfileScreenCallbacks(
                    onOrderClick = {},
                    onProductClick = {},
                    onCreateListing = {},
                    onEditListing = {},
                    onManageAddresses = {},
                    onBuyerMessagesClick = {},
                    onSellerMessagesClick = {},
                    onFavoritesClick = {},
                ),
                viewModel = ScreenTestViewModels.guestProfile(),
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("我的收藏").assertExists()
        composeRule.onNodeWithText("我的消息").assertExists()
        composeRule.onNodeWithText("收到的消息").assertExists()
    }

    @Test
    fun profileScreen_signedIn_showsDisplayName() {
        composeRule.setContent {
            ProfileScreen(
                callbacks = ProfileScreenCallbacks(
                    onOrderClick = {},
                    onProductClick = {},
                    onCreateListing = {},
                    onEditListing = {},
                    onManageAddresses = {},
                    onBuyerMessagesClick = {},
                    onSellerMessagesClick = {},
                    onFavoritesClick = {},
                ),
                viewModel = ScreenTestViewModels.signedInProfile(displayName = "张三"),
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("张三").assertExists()
    }
}
