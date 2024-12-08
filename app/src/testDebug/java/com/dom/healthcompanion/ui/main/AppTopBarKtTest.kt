package com.dom.healthcompanion.ui.main

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import com.dom.healthcompanion.R
import com.dom.healthcompanion.utils.IconState
import com.dom.healthcompanion.utils.TextString
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppTopBarKtTest {
    // region test cases

    // 1- When shown, given there is a title in topAppBarTextFlow, then show correct title
    // 2- When shown, given there is no title in topAppBarTextFlow, then show app name
    // 3- When shown, given there is an icon in iconStateFlow, then show icon
    // 4- When shown, given there is an icon in iconStateFlow but isVisible is false, then do not show icon
    // 5- When shown, given there is no icon in iconStateFlow, then show no icon

    // 6- When title flow updated, then show correct title

    // 7- When iconStateFlow updated, given isVisible from false to true, then show icon
    // 8- When iconStateFlow updated, given isVisible from true to false, then dont show icon
    // 9- When iconStateFlow updated, given isVisible stays false, then don't change visibility of icon
    // 10- When iconStateFlow updated, given isVisible stays true, then don't change visibility of icon
    // 11- When iconStateFlow updated, given icon type is updated, then show new icon vector
    // 12- When iconStateFlow updated, given onClick changes, then use new onClick for IconButton

    // 13- When IconButton clicked, given there is onClick action in iconStateFlow, then invoke onClick function

    // endregion

    @get:Rule
    val composeTestRule = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var defaultTopAppBarTextFlow: Flow<TextString>
    private lateinit var defaultIconStateFlow: Flow<IconState>

    @Before
    fun setUp() {
        // Redirect Logcat to console
        ShadowLog.stream = System.out
        defaultTopAppBarTextFlow = MutableStateFlow(TextString.String(""))
        defaultIconStateFlow = MutableStateFlow(IconState(false, IconState.Type.BACK) {})
    }

    // region When shown
    @Test
    fun `1- When shown, given there is a title in topAppBarTextFlow, then show correct title`() {
        // Arrange
        val expectedText = "test title"
        defaultTopAppBarTextFlow = flowOf(TextString.String(expectedText))
        // Act
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, defaultIconStateFlow)
        }
        // Assert
        composeTestRule.onNodeWithTag("topBarTitle")
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }

    @Test
    fun `2- When shown, given there is no title in topAppBarTextFlow, then show app name`() {
        // Arrange
        val titleFlow = emptyFlow<TextString>()
        // Act
        composeTestRule.setContent {
            AppTopBar(titleFlow, defaultIconStateFlow)
        }
        // Assert
        composeTestRule.onNodeWithTag("topBarTitle")
            .assertIsDisplayed()
            .assertTextEquals(context.getString(R.string.app_name))
    }

    @Test
    fun `3- When shown, given there is an icon in iconStateFlow, then show icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        defaultIconStateFlow = flowOf(IconState(true, expectedIcon) {})
        // Act
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, defaultIconStateFlow)
        }
        // Assert
        composeTestRule.onNodeWithTag(expectedIcon.vector.toString(), useUnmergedTree = true)
            .assertContentDescriptionEquals("topBarIcon")
            .assertIsDisplayed()
    }

    @Test
    fun `4- When shown, given there is an icon in iconStateFlow but isVisible is false, then do not show icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        defaultIconStateFlow = flowOf(IconState(false, expectedIcon) {})
        // Act
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, defaultIconStateFlow)
        }
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .assertDoesNotExist()
    }

    @Test
    fun `5- When shown, given there is no icon in iconStateFlow, then show no icon`() {
        // Arrange
        val iconFlow = emptyFlow<IconState>()
        // Act
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .assertDoesNotExist()
    }
    // endregion

    // region When title flow updated
    @Test
    fun `6- When title flow updated, then show correct title`() {
        // Arrange
        val expectedText = "test title"
        val titleFlow = MutableStateFlow(TextString.String(""))
        composeTestRule.setContent {
            AppTopBar(titleFlow, defaultIconStateFlow)
        }
        // Act
        titleFlow.value = TextString.String(expectedText)
        // Assert
        composeTestRule.onNodeWithTag("topBarTitle")
            .assertIsDisplayed()
            .assertTextEquals(expectedText)
    }
    // endregion

    // region When iconStateFlow updated
    @Test
    fun `7- When iconStateFlow updated, given isVisible from false to true, then show icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        val iconFlow = MutableStateFlow(IconState(false, expectedIcon) {})
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        iconFlow.value = iconFlow.value.copy(isVisible = true)
        // Assert
        composeTestRule.onNodeWithTag(expectedIcon.vector.toString(), useUnmergedTree = true)
            .assertContentDescriptionEquals("topBarIcon")
            .assertIsDisplayed()
    }

    @Test
    fun `8- When iconStateFlow updated, given isVisible from true to false, then dont show icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        val iconFlow = MutableStateFlow(IconState(true, expectedIcon) {})
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        iconFlow.value = iconFlow.value.copy(isVisible = false)
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .assertDoesNotExist()
    }

    @Test
    fun `9- When iconStateFlow updated, given isVisible stays false, then don't change visibility of icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        val iconFlow = MutableStateFlow(IconState(false, expectedIcon) {})
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        iconFlow.value = iconFlow.value.copy(isVisible = false)
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .assertDoesNotExist()
    }

    @Test
    fun `10- When iconStateFlow updated, given isVisible stays true, then don't change visibility of icon`() {
        // Arrange
        val expectedIcon = IconState.Type.BACK
        val iconFlow = MutableStateFlow(IconState(true, expectedIcon) {})
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        iconFlow.value = iconFlow.value.copy(isVisible = true)
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .assertIsDisplayed()
    }

    @Test
    fun `11- When iconStateFlow updated, given icon type is updated, then show new icon vector`() {
        // Arrange
        val expectedIcon = IconState.Type.LIST
        val iconFlow = MutableStateFlow(IconState(true, IconState.Type.BACK) {})
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("TAG")
        // Act
        iconFlow.value = iconFlow.value.copy(iconType = expectedIcon)
        // Assert
        composeTestRule.onNodeWithTag(expectedIcon.vector.toString(), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `12- When iconStateFlow updated, given onClick changes, then use new onClick for IconButton`() {
        // Arrange
        val oldOnClick = mockk<() -> Unit>()
        val newOnClick = mockk<() -> Unit>(relaxed = true)
        val iconFlow = MutableStateFlow(IconState(true, IconState.Type.BACK, oldOnClick))
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        iconFlow.value = iconFlow.value.copy(onClick = newOnClick)
        // Assert
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .performClick()
        verify { newOnClick() }
        verify(exactly = 0) { oldOnClick() }
    }
    // endregion

    @Test
    fun `13- When IconButton clicked, given there is onClick action in iconStateFlow, then invoke onClick function`() {
        // Arrange
        val onClick = mockk<() -> Unit>(relaxed = true)
        val iconFlow = MutableStateFlow(IconState(true, IconState.Type.BACK, onClick))
        composeTestRule.setContent {
            AppTopBar(defaultTopAppBarTextFlow, iconFlow)
        }
        // Act
        composeTestRule.onNodeWithContentDescription("topBarIcon")
            .performClick()
        // Assert
        verify { onClick() }
    }
}
