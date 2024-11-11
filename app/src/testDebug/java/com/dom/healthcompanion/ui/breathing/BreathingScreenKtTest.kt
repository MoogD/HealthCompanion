package com.dom.healthcompanion.ui.breathing

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.TestTags
import com.dom.healthcompanion.utils.ButtonState
import com.dom.healthcompanion.utils.TextString
import com.dom.logger.Logger
import com.dom.testUtils.assertCorrectTextShown
import com.dom.testUtils.assertCountForTag
import com.dom.testUtils.getProgressIndicatorProgress
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlin.jvm.Throws
import kotlinx.coroutines.flow.MutableStateFlow
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
class BreathingScreenKtTest {
    // region test cases

    // 1- When shown, then show correct title, timerState and buttonState

    // 2- When titleFlow updated, then show correct title

    // 3- When timerState updated, given new timer type is IDLE, then show correct type
    // 4- When timerState updated, given new timer type is INHALE, then show correct type
    // 5- When timerState updated, given new timer type is EXHALE, then show correct type
    // 6- When timerState updated, given new timer type is HOLD, then show correct type
    // 7- When timerState updated, given new timer type is PAUSE, then show correct type
    // 8- When timerState updated, given new timer type is LOWER_BREATHING, then show correct type
    // 9- When timerState updated, given new timer type is NORMAL_BREATHING, then show correct type
    // 10- When timerState updated, given new timer type is FINISHED, then show correct type
    // 11- When timerState updated, given current time updated, then show correct current time
    // 12- When timerState updated, given total time updated, then show correct total time
    // 13- When timerState updated, given progress updated, then show correct progress
    // 14- When timerState updated, given lap added, then show correct laps
    // 15- When timerState updated, given lap removed, then show correct laps
    // 16- When timerState updated, given lap updated, then show correct laps

    // 17- When buttonState onClick invoked, then invoke onClick
    // 18- When buttonState updated, given onClick changed, then update button onClick function
    // 19- When buttonState updated, given text changed, then update button text

    // 20- When onStop button clicked, then invoke onStopClicked

    // endregion

    @get:Rule
    val composeTestRule = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val logger = mockk<Logger>(relaxed = true)

    @Before
    @Throws(Exception::class)
    fun setup() {
        // Redirect Logcat to console
        ShadowLog.stream = System.out
    }

    private val titleFlow = MutableStateFlow(TextString.String("test"))
    private val timerStateFlow =
        MutableStateFlow(
            TimerState(
                BreathingExercise.RoundType.FINISHED,
                BreathingViewModel.STARTING_TIME_STRING,
                BreathingViewModel.STARTING_TIME_STRING,
                0f,
            ),
        )
    private val buttonStateFlow = MutableStateFlow(ButtonState(TextString.String("test")) {})
    private val onStopClicked = mockk<() -> Unit>()

    @Test
    fun `1- When shown, then show correct title, timerState and buttonState`() {
        // Arrange
        val expectedTitleText = "TestTitle123"
        titleFlow.value = TextString.String(expectedTitleText)
        val expectedTimerState =
            TimerState(
                BreathingExercise.RoundType.LOWER_BREATHING,
                BreathingViewModel.STARTING_TIME_STRING,
                BreathingViewModel.STARTING_TIME_STRING,
                0f,
            )
        timerStateFlow.value = expectedTimerState
        val buttonStateText = "testButtonState"
        val expectedButtonState = ButtonState(TextString.String(buttonStateText)) {}
        buttonStateFlow.value = expectedButtonState
        // Act
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Assert
        // assert title shown
        composeTestRule.assertCorrectTextShown(expectedTitleText, TestTags.TITLE_TEXT_TAG)
        // Assert timer state
        composeTestRule.assertCorrectTextShown(expectedTimerState.type.name, TestTags.TIMER_TYPE_TEXT_TAG)
        composeTestRule.assertCorrectTextShown(expectedTimerState.currentTimeText, TestTags.TIMER_CURRENT_TIME_TEXT_TAG)
        val progress = composeTestRule.getProgressIndicatorProgress()
        assertThat(progress, `is`(expectedTimerState.progress))
        composeTestRule.assertCorrectTextShown(expectedTimerState.totalTimeText, TestTags.TIMER_TOTAL_TIME_TEXT_TAG)
        composeTestRule.assertCountForTag(expectedTimerState.laps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
        // assert button states
        composeTestRule.onNodeWithText(context.getString(R.string.btnStopText)).assertIsDisplayed()
        composeTestRule.onNodeWithText(buttonStateText).assertIsDisplayed()
    }

    @Test
    fun `2- When titleFlow updated, then show correct title`() {
        // Arrange
        val expectedTitleText = "TestTitleNew"
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        titleFlow.update { TextString.String(expectedTitleText) }
        // Assert
        composeTestRule.assertCorrectTextShown(expectedTitleText, TestTags.TITLE_TEXT_TAG)
    }

    // region timerState updated
    @Test
    fun `3- When timerState updated, given new timer type is IDLE, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.IDLE
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `4- When timerState updated, given new timer type is INHALE, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.INHALE
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `5- When timerState updated, given new timer type is EXHALE, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.EXHALE
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `6- When timerState updated, given new timer type is HOLD, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.HOLD
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `7- When timerState updated, given new timer type is PAUSE, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.PAUSE
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `8- When timerState updated, given new timer type is LOWER_BREATHING, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.IDLE) }
        val newType = BreathingExercise.RoundType.LOWER_BREATHING
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `9- When timerState updated, given new timer type is NORMAL_BREATHING, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.NORMAL_BREATHING
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `10- When timerState updated, given new timer type is FINISHED, then show correct type`() {
        // Arrange
        timerStateFlow.update { timerStateFlow.value.copy(BreathingExercise.RoundType.LOWER_BREATHING) }
        val newType = BreathingExercise.RoundType.FINISHED
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(newType) }
        // Assert
        composeTestRule.assertCorrectTextShown(newType.name, TestTags.TIMER_TYPE_TEXT_TAG)
    }

    @Test
    fun `11- When timerState updated, given current time updated, then show correct current time`() {
        // Arrange
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        val expectedText = "01:00"
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(currentTimeText = expectedText) }
        // Assert
        composeTestRule.assertCorrectTextShown(expectedText, TestTags.TIMER_CURRENT_TIME_TEXT_TAG)
    }

    @Test
    fun `12- When timerState updated, given total time updated, then show correct total time`() {
        // Arrange
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        val expectedText = "01:00"
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(totalTimeText = expectedText) }
        // Assert
        composeTestRule.assertCorrectTextShown(expectedText, TestTags.TIMER_TOTAL_TIME_TEXT_TAG)
    }

    @Test
    fun `13- When timerState updated, given progress updated, then show correct progress`() {
        // Arrange
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        val newProgress = 0.5f
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(progress = newProgress) }
        // Assert
        val progress = composeTestRule.getProgressIndicatorProgress()
        assertThat(progress, `is`(newProgress))
    }

    @Test
    fun `14- When timerState updated, given lap added, then show correct laps`() {
        // Arrange
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        val newLaps = timerStateFlow.value.laps.toMutableList()
        val newIndex = 100
        val newLapTime = "02:00"
        newLaps.add(TimerLap(newIndex, newLapTime))
        // Act
        timerStateFlow.update { timerStateFlow.value.copy(laps = newLaps) }
        // Assert
        composeTestRule.assertCountForTag(newLaps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
        composeTestRule
            .onAllNodesWithTag(TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
            .assertAny(hasText("$newIndex: $newLapTime"))
    }

    @Test
    fun `15- When timerState updated, given lap removed, then show correct laps`() {
        // Arrange
        val laps = timerStateFlow.value.laps.toMutableList()
        laps.add(TimerLap(laps.lastIndex + 1, "01:00"))
        timerStateFlow.update { timerStateFlow.value.copy(laps = laps) }
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        composeTestRule.assertCountForTag(laps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)

        // Act
        val newLaps = laps.toMutableList()
        val lap = newLaps.removeLast()
        timerStateFlow.update { timerStateFlow.value.copy(laps = newLaps) }
        // Assert
        composeTestRule.assertCountForTag(newLaps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
    }

    @Test
    fun `16- When timerState updated, given lap updated, then show correct laps`() {
        // Arrange
        val laps = timerStateFlow.value.laps.toMutableList()
        laps.add(TimerLap(100, "01:00"))
        timerStateFlow.update { timerStateFlow.value.copy(laps = laps) }
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        composeTestRule.assertCountForTag(laps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)

        // Act
        val newLaps = laps.toMutableList()
        val lap = newLaps.removeLast()
        val newIndex = lap.index + 2
        val newLapTime = "10:01"
        newLaps.add(TimerLap(newIndex, newLapTime))
        timerStateFlow.update { timerStateFlow.value.copy(laps = newLaps) }
        // Assert
        composeTestRule.assertCountForTag(newLaps.size, TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
        composeTestRule
            .onAllNodesWithTag(TestTags.TIMER_LAP_LIST_TEXT_ITEM_TAG)
            .assertAny(hasText("$newIndex: $newLapTime"))
    }
    // endregion

    // region buttonState

    @Test
    fun `17- When buttonState onClick invoked, then invoke onClick`() {
        // Arrange
        val onClick = mockk<() -> Unit>(relaxed = true)
        buttonStateFlow.update { buttonStateFlow.value.copy(onClick = onClick, text = TextString.Res(R.string.btnStartText)) }
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        composeTestRule.onNodeWithText(context.getString(R.string.btnStartText)).performClick()
        // Assert
        verify { onClick() }
    }

    @Test
    fun `18- When buttonState updated, given onClick changed, then update button onClick function`() {
        // Arrange
        val onClick = mockk<() -> Unit>()
        buttonStateFlow.update { buttonStateFlow.value.copy(onClick = onClick, text = TextString.Res(R.string.btnStartText)) }
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        val newOnClick = mockk<() -> Unit>(relaxed = true)
        // Act
        buttonStateFlow.update { buttonStateFlow.value.copy(onClick = newOnClick) }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.btnStartText)).performClick()
        verify(exactly = 0) { onClick() }
        verify { newOnClick() }
    }

    @Test
    fun `19- When buttonState updated, given text changed, then update button text`() {
        // Arrange
        val onClick = mockk<() -> Unit>()
        buttonStateFlow.update { buttonStateFlow.value.copy(onClick = onClick, text = TextString.Res(R.string.btnStartText)) }
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        // Act
        buttonStateFlow.update { buttonStateFlow.value.copy(text = TextString.Res(R.string.btnPauseText)) }
        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.btnStartText)).isNotDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.btnPauseText)).isDisplayed()
    }

    // endregion

    @Test
    fun `20- When onStop button clicked, then invoke onStopClicked`() {
        // Arrange
        composeTestRule.setContent {
            BreathingScreen(
                titleFlow = titleFlow,
                timerStateFlow = timerStateFlow,
                startButtonStateFlow = buttonStateFlow,
                onStopClicked,
                logger,
            )
        }
        justRun { onStopClicked() }
        // Act
        composeTestRule.onNodeWithText(context.getString(R.string.btnStopText)).performClick()
        // Assert
        verify { onStopClicked() }
    }
}
