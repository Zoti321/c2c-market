package com.zoti321.c2cmarket.ui.home

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
class HomeScreenTest {

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
    fun homeScreen_showsTitle() {
        composeRule.setContent {
            HomeScreen(
                onProductClick = {},
                onSearchClick = {},
                viewModel = ScreenTestViewModels.home(),
            )
        }
        composeRule.onNodeWithText("首页").assertExists()
    }
}
