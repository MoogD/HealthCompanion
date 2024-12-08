package com.dom.healthcompanion.utils

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.dom.healthcompanion.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TextStringKtTest {
    // region test cases

    // 1- When getAsString invoked, given Text is TextRes, then return correct string fro resId
    // 2- When getAsString invoked, given Text is TextString, then return correct string directly

    // endregion
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `1- When getAsString invoked, given Text is TextRes, then return correct string fro resId`() {
        // Arrange
        val textRes = R.string.app_name
        val sut = TextString.Res(textRes)
        val expectedString = InstrumentationRegistry.getInstrumentation().targetContext.getString(textRes)
        // Act
        composeTestRule.setContent {
            val result = sut.getAsString()
            // Assert
            assertThat(result, `is`(expectedString))
        }
    }

    @Test
    fun `2- When getAsString invoked, given Text is TextString, then return correct string directly`() {
        // Arrange

        val expectedText = "test2131"
        val sut = TextString.String(expectedText)
        // Act
        composeTestRule.setContent {
            val result = sut.getAsString()
            // Assert
            assertThat(result, `is`(expectedText))
        }
    }
}
