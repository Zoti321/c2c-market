package com.zoti321.c2cmarket.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_showsTitle() {
        composeRule.setContent {
            HomeScreen(
                onProductClick = {},
                onSearchClick = {},
            )
        }
        composeRule.onNodeWithText("首页").assertExists()
    }
}
