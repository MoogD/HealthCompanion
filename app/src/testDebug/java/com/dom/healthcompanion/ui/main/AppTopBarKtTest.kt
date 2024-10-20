package com.dom.healthcompanion.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppTopBarKtTest {
    // region test cases

    // 1- When shown, then show correct title
    // 2- When back icon pressed, then invoke provided onBackPressed function

    // endregion

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `1- When shown, then show correct title`() {
        // Arrange
        val topAppBarText = "Top App Bar"
        // Act
        composeTestRule.setContent {
            AppTopBar(topAppBarText, {})
        }
        // Assert
        composeTestRule.onNodeWithText(topAppBarText).assertIsDisplayed()
    }

    @Test
    fun `2- When back icon pressed, then invoke provided onBackPressed function`() {
        // Arrange
        val topAppBarText = "Top App Bar"
        val onBackPressed = mockk<() -> Unit>(relaxed = true)
        composeTestRule.setContent {
            AppTopBar(topAppBarText, onBackPressed)
        }
        // Act
        composeTestRule.onNodeWithContentDescription("backIcon").performClick()
        // Assert
        verify { onBackPressed() }
    }
}
