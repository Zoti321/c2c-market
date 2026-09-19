package com.zoti321.c2cmarket.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun profileScreen_showsGuestMode() {
        composeRule.setContent {
            ProfileScreen(
                onOrderClick = {},
                onProductClick = {},
                onCreateListing = {},
                onEditListing = {},
                onManageAddresses = {},
            )
        }
        composeRule.onNodeWithText("游客模式").assertExists()
    }
}
