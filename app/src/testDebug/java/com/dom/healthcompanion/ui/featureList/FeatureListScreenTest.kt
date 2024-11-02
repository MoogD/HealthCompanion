package com.dom.healthcompanion.ui.featureList

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.dom.healthcompanion.R
import kotlin.jvm.Throws
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class FeatureListScreenTest {
    // region test cases

    // 1- When shown, given provided flow contains featureItems, then show one text for each item

    // 2- When featureItemFlow is updated, given item is removed from flow, then text items are updated
    // 3- When featureItemFlow is updated, given item is added to flow, then text items are updated
    // 4- When featureItemFlow is updated, given items are replaced in flow, then text items are updated
    // 5- When featureItemFlow is updated, given items are cleared from flow, then text items are updated

    // 6- When an item in row is clicked, then trigger correct onClick lambda

    // endregion

    @get:Rule
    val composeTestRule = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    @Throws(Exception::class)
    fun setup() {
        // Redirect Logcat to console
        ShadowLog.stream = System.out
    }

    @Test
    fun `1- When shown, given provided flow contains featureItems, then show one text for each item`() {
        // Arrange
        val featureFlow =
            flowOf(
                listOf(
                    FeatureItem(R.string.breathing_screen_title) {},
                    FeatureItem(R.string.btnNextText) {},
                    FeatureItem(R.string.app_name) {},
                ),
            )
        // Act
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        val rowCount = composeTestRule.onAllNodesWithTag("featureItem").fetchSemanticsNodes().size
        assertThat(rowCount, `is`(3))
    }

    @Test
    fun `2- When featureItemFlow is updated, given item is removed from flow, then text items are updated`() {
        // Arrange
        val items =
            listOf(
                FeatureItem(R.string.breathing_screen_title) {},
                FeatureItem(R.string.btnNextText) {},
                FeatureItem(R.string.app_name) {},
            )
        val featureFlow = MutableStateFlow(items)
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        // Act
        val updatedList = items.toMutableList()
        updatedList.removeLast()
        featureFlow.update { updatedList }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsNotDisplayed()
        val rowCount = composeTestRule.onAllNodesWithTag("featureItem").fetchSemanticsNodes().size
        assertThat(rowCount, `is`(2))
    }

    @Test
    fun `3- When featureItemFlow is updated, given item is added to flow, then text items are updated`() {
        // Arrange
        val items =
            listOf(
                FeatureItem(R.string.breathing_screen_title) {},
                FeatureItem(R.string.btnNextText) {},
                FeatureItem(R.string.app_name) {},
            )
        val featureFlow = MutableStateFlow(items)
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        // Act
        val updatedList = items.toMutableList()
        updatedList.add(FeatureItem(R.string.btnPauseText) {})
        featureFlow.update { updatedList }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnPauseText)).assertIsDisplayed()
        val rowCount = composeTestRule.onAllNodesWithTag("featureItem").fetchSemanticsNodes().size
        assertThat(rowCount, `is`(4))
    }

    @Test
    fun `4- When featureItemFlow is updated, given items are replaced in flow, then text items are updated`() {
        // Arrange
        val items =
            listOf(
                FeatureItem(R.string.breathing_screen_title) {},
                FeatureItem(R.string.btnNextText) {},
                FeatureItem(R.string.app_name) {},
            )
        val featureFlow = MutableStateFlow(items)
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        // Act
        val updatedList = items.toMutableList()
        updatedList.removeLast()
        updatedList.add(FeatureItem(R.string.btnPauseText) {})
        featureFlow.update { updatedList }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnPauseText)).assertIsDisplayed()
        val rowCount = composeTestRule.onAllNodesWithTag("featureItem").fetchSemanticsNodes().size
        assertThat(rowCount, `is`(3))
    }

    @Test
    fun `5- When featureItemFlow is updated, given items are cleared from flow, then text items are updated`() {
        // Arrange
        val items =
            listOf(
                FeatureItem(R.string.breathing_screen_title) {},
                FeatureItem(R.string.btnNextText) {},
                FeatureItem(R.string.app_name) {},
            )
        val featureFlow = MutableStateFlow(items)
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        // Act
        val updatedList = items.toMutableList()
        updatedList.clear()
        featureFlow.update { updatedList }
        // Assert
        val rowCount = composeTestRule.onAllNodesWithTag("featureItem").fetchSemanticsNodes().size
        assertThat(rowCount, `is`(0))
    }

    @Test
    fun `6- When an item in row is clicked, then trigger correct onClick lambda`() {
        // Arrange
        var firstItemClickCount = 0
        var secondItemClickCount = 0
        var thirdItemClickCount = 0
        var fourthItemClickCount = 0
        val items =
            listOf(
                FeatureItem(R.string.breathing_screen_title) { firstItemClickCount++ },
                FeatureItem(R.string.btnNextText) { secondItemClickCount++ },
                FeatureItem(R.string.app_name) { thirdItemClickCount++ },
                FeatureItem(R.string.btnPauseText) { fourthItemClickCount++ },
            )
        val featureFlow = MutableStateFlow(items)
        composeTestRule.setContent {
            FeatureListScreen(featureFlow)
        }
        // Act
        composeTestRule.onNodeWithText(context.getString(R.string.breathing_screen_title)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.btnNextText)).performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.btnPauseText)).performClick()

        // Assert
        assertThat(firstItemClickCount, `is`(1))
        assertThat(secondItemClickCount, `is`(1))
        assertThat(thirdItemClickCount, `is`(0))
        assertThat(fourthItemClickCount, `is`(1))
    }
}
