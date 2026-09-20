package com.zoti321.c2cmarket.ui.cart

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.zoti321.c2cmarket.HiltTestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class CartScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun cartScreen_showsEmptyState() {
        composeRule.setContent {
            CartScreen(
                onCheckout = {},
                onGoHome = {},
            )
        }
        composeRule.onNodeWithText("购物车是空的").assertExists()
    }
}
