package com.dom.healthcompanion.ui.breathing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.dom.healthcompanion.R
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise
import com.dom.healthcompanion.domain.breathing.model.ButeykoBreathing
import com.dom.healthcompanion.domain.breathing.usecase.GetCurrentBreathingExerciseUseCase
import com.dom.healthcompanion.utils.Text
import com.dom.testUtils.TestDispatcherProvider
import com.dom.timer.CountUpTimer
import com.dom.timer.CountUpTimerImpl
import com.dom.timer.millisToMinutesAndSeconds
import io.mockk.EqMatcher
import io.mockk.clearAllMocks
import io.mockk.clearConstructorMockk
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BreathingViewModelTest {
    // region test cases

    // 1- When initialized, then show title of current exercise
    // 2- When initialized, then show timerState with idle round type, starting time for current and total time, 0 progress and no laps
    // 3- When initialized, then show start button state

    // 6- When start button state clicked, given first round is open timer, then setup and start timer without endTime
    // 7- When start button state clicked, given first round has endTime and there is no next round, then setup and start timer with endTime from round
    // 8- When start button state clicked, given first round has endTime and there is next round that does not start automatically, then setup and start timer without endTime
    // 9- When start button state clicked, given first round has endTime and there is next round that does start automatically, then setup and start timer with endTime from round
    // 10- When start button state clicked, given there is next round that starts automatically and current rounds has endTime, then show pause button state
    // 11- When start button state clicked, given there is next round that needs to be started by user and current round is open timer, then show next button state
    // 12- When start button state clicked, given there is next round that needs to be started by user and current round is not open timer, then show pause button state
    // 13- When start button state clicked, given there is no next round and current round is open timer, then show next button state
    // 14- When start button state clicked, given there is no next round and current round is not open timer, then show pause button state
    // TODO: Add cleanup check for timerState
    // 15- When start button state clicked, given there was timer running before, then reset the values and clean up previous timer

    // 16- When next button state clicked, then call stop on timer
    // 17- When next button state clicked, given current round is open timer, then add passed time from timer to laps in timerState
    // 18- When next button state clicked, given current round is not open timer and there is next round that does not start automatically, then add passed time from timer to laps in timerState
    // 19- When next button state clicked, given there is next round, then update currentRoundIndex and timerState title and create and start new timer for next Round
    // 20- When next button state clicked, given there is no next round, then show finished timerState and start button
    // 21- When next button state clicked, given there is next round that is open timer, then show next button state
    // 22- When next button state clicked, given there is next round that is not open timer, then show pause button state

    // 23- When onTick invoked, given current round has endTime and endTime is reached, then show next button state
    // 24- When onTick invoked, given current round had endTime that is not reached yet, then do not change button
    // 25- When onTick invoked, given current round is open timer, then do not change button
    // 26- When onTick invoked, given current round is open timer, then set progress 0 and update currentTimeText and totalTimeText in timerState
    // 27- When onTick invoked, given current round has endTime, then update progress, currentTimeText and totalTimeText in timerState
    // 28- When onTick invoked, given there were already rounds finished, then set totalTime current timer time to time of previous rounds

    // 29- When onFinish invoked, given current round is open timer, then add passed time from timer to laps in timerState
    // 30- When onFinish invoked, given current round is not open timer and there is next round that does not start automatically, then add passed time from timer to laps in timerState
    // 31- When onFinish invoked, given there is next round, then update currentRoundIndex and timerState title and create and start new timer for next Round
    // 32- When onFinish invoked, given there is no next round, then show finished timerState and start button
    // 33- When onFinish invoked, given there is next round that is open timer, then show next button state
    // 34- When onFinish invoked, given there is next round that is not open timer, then show pause button state

    // 35- When onCleared invoked, given there is timer, then call stop on timer and remove listeners
    // 36- When onCleared invoked, given there is no timer, then nothing happens

    // 37- When pause button state clicked, then call pause on timer and show resume button state

    // 38- When resume button state clicked, then call resume on timer and show pause button state

    // endregion
    val defaultExercise = ButeykoBreathing()
    private val getCurrentBreathingExerciseUseCase: GetCurrentBreathingExerciseUseCase = mockk()

    private val testDispatcher = TestDispatcherProvider()

    private lateinit var sut: BreathingViewModel

    @BeforeEach
    fun setUp() {
        every { getCurrentBreathingExerciseUseCase() } returns defaultExercise
        sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("When initialized")
    inner class Initialized {
        @Test
        fun `1- then show title of current exercise`() {
            // Assert
            val title = sut.titleFlow.value
            assertThat(title, `is`(defaultExercise.title))
        }

        @Test
        fun `2- then show timerState with idle round type, starting time for current and total time, 0 progress and no laps`() {
            // Assert
            val timerState = sut.timerStateFlow.value
            assertThat(timerState.type, `is`(BreathingExercise.RoundType.IDLE))
            assertThat(timerState.currentTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
            assertThat(timerState.totalTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
            assertThat(timerState.progress, `is`(0f))
            assertThat(timerState.laps, `is`(emptyList()))
        }

        @Test
        fun `3- When initialized, then show start button state`() {
            // Assert
            val buttonState = sut.buttonStateFlow.value
            assertThat(buttonState.text, `is`(Text.TextRes(R.string.btnStartText)))
        }
    }

    @Nested
    @DisplayName("When start button clicked")
    inner class StartButtonClicked {
        @Test
        fun `6- given first round is open timer, then setup and start timer without endTime`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    BreathingExercise.OPEN_TIMER,
                                    BreathingExercise.RoundType.HOLD,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // Act
                sut.buttonStateFlow.value.onClick.invoke()
                // Assert
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(CountUpTimer.NO_END_TIME),
                        EqMatcher(testDispatcher),
                    ).setListener(any())
                }
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(CountUpTimer.NO_END_TIME),
                        EqMatcher(testDispatcher),
                    ).start()
                }
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `7- given first round has endTime and there is no next round, then setup and start timer with endTime from round`(expectedEndTime: Long) =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    expectedEndTime,
                                    BreathingExercise.RoundType.HOLD,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // Act
                sut.buttonStateFlow.value.onClick.invoke()
                // Assert
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(expectedEndTime),
                        EqMatcher(testDispatcher),
                    ).setListener(any())
                }
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(expectedEndTime),
                        EqMatcher(testDispatcher),
                    ).start()
                }
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `8- given first round has endTime and there is next round that does not start automatically, then setup and start timer without endTime`(expectedEndTime: Long) {
            // Arrange
            mockkConstructor(CountUpTimerImpl::class)
            val expectedExercise =
                object : BreathingExercise {
                    override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                    override val rounds: List<BreathingExercise.BreathingRound> =
                        listOf(
                            BreathingExercise.BreathingRound(
                                expectedEndTime,
                                BreathingExercise.RoundType.HOLD,
                                false,
                            ),
                            BreathingExercise.BreathingRound(
                                expectedEndTime,
                                BreathingExercise.RoundType.LOWER_BREATHING,
                                false,
                            ),
                        )
                    override var currenRoundIndex: Int = 0
                }
            every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
            sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
            // Act
            sut.buttonStateFlow.value.onClick.invoke()
            // Assert
            verify {
                constructedWith<CountUpTimerImpl>(
                    EqMatcher(0L),
                    EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                    EqMatcher(CountUpTimer.NO_END_TIME),
                    EqMatcher(testDispatcher),
                ).setListener(any())
            }
            verify {
                constructedWith<CountUpTimerImpl>(
                    EqMatcher(0L),
                    EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                    EqMatcher(CountUpTimer.NO_END_TIME),
                    EqMatcher(testDispatcher),
                ).start()
            }
        }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `9- given first round has endTime and there is next round that does start automatically, then setup and start timer with endTime from round`(expectedEndTime: Long) {
            // Arrange
            mockkConstructor(CountUpTimerImpl::class)
            val expectedExercise =
                object : BreathingExercise {
                    override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                    override val rounds: List<BreathingExercise.BreathingRound> =
                        listOf(
                            BreathingExercise.BreathingRound(
                                expectedEndTime,
                                BreathingExercise.RoundType.HOLD,
                                false,
                            ),
                            BreathingExercise.BreathingRound(
                                1000L,
                                BreathingExercise.RoundType.LOWER_BREATHING,
                                true,
                            ),
                        )
                    override var currenRoundIndex: Int = 0
                }
            every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
            sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
            // Act
            sut.buttonStateFlow.value.onClick.invoke()
            // Assert
            verify {
                constructedWith<CountUpTimerImpl>(
                    EqMatcher(0L),
                    EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                    EqMatcher(expectedEndTime),
                    EqMatcher(testDispatcher),
                ).setListener(any())
            }
            verify {
                constructedWith<CountUpTimerImpl>(
                    EqMatcher(0L),
                    EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                    EqMatcher(expectedEndTime),
                    EqMatcher(testDispatcher),
                ).start()
            }
        }

        @Test
        fun `10- given there is next round that starts automatically and current rounds has endTime, then show pause button state`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    true,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                sut.buttonStateFlow.test {
                    // catch initial state
                    awaitItem()
                    // Act
                    sut.buttonStateFlow.value.onClick.invoke()
                    // Assert
                    assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnPauseText)))
                }
            }

        @Test
        fun `11- given there is next round that needs to be started by user and current round is open timer, then show next button state`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    BreathingExercise.OPEN_TIMER,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                sut.buttonStateFlow.test {
                    // catch initial state
                    awaitItem()
                    // Act
                    sut.buttonStateFlow.value.onClick.invoke()
                    // Assert
                    assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnNextText)))
                }
            }

        @Test
        fun `12- given there is next round that needs to be started by user and current round is not open timer, then show pause button state`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                sut.buttonStateFlow.test {
                    // catch initial state
                    awaitItem()
                    // Act
                    sut.buttonStateFlow.value.onClick.invoke()
                    // Assert
                    assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnPauseText)))
                }
            }

        @Test
        fun `13- given there is no next round and current round is open timer, then show next button state`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    BreathingExercise.OPEN_TIMER,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                sut.buttonStateFlow.test {
                    // catch initial state
                    awaitItem()
                    // Act
                    sut.buttonStateFlow.value.onClick.invoke()
                    // Assert
                    assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnNextText)))
                }
            }

        @Test
        fun `14- given there is no next round and current round is not open timer, then show pause button state`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                sut.buttonStateFlow.test {
                    // catch initial state
                    awaitItem()
                    // Act
                    sut.buttonStateFlow.value.onClick.invoke()
                    // Assert
                    assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnPauseText)))
                }
            }

        @Test
        fun `15- given there was timer running before, then reset the values`() =
            runTest {
                // Arrange
                val listenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(listenerSlot)) }
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    200L,
                                    BreathingExercise.RoundType.HOLD,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    true,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } answers {
                    // reset index to 0 on each invocation to simulate recreating the exercise
                    expectedExercise.currenRoundIndex = 0
                    return@answers expectedExercise
                }
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)

                clearMocks(getCurrentBreathingExerciseUseCase, answers = false)
                // start timer by button press
                sut.buttonStateFlow.value.onClick.invoke()
                // simulate all timers done by listener calls
                expectedExercise.rounds.forEach {
                    listenerSlot.captured.onFinish(it.expectedTime)
                }

                // Act
                sut.buttonStateFlow.value.onClick.invoke()
                // Assert
                verify { getCurrentBreathingExerciseUseCase() }
                val timerState = sut.timerStateFlow.value
                assertThat(timerState.laps, `is`(emptyList()))
                assertThat(timerState.type, `is`(expectedExercise.rounds.first().type))
                assertThat(timerState.totalTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
                assertThat(timerState.currentTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
                assertThat(timerState.progress, `is`(0f))
            }
    }

    @Nested
    @DisplayName("When next button state clicked")
    inner class NextButtonClicked {
        @Test
        fun `16- then call stop on timer`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    BreathingExercise.OPEN_TIMER,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                clearConstructorMockk(CountUpTimerImpl::class)
                // Act
                sut.buttonStateFlow.value.onClick()
                // Assert
                verify { anyConstructed<CountUpTimerImpl>().stop() }
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `17- given current round is open timer, then add passed time from timer to laps in timerState`(passedTime: Long) =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    BreathingExercise.OPEN_TIMER,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                val previousRounds = sut.timerStateFlow.value.laps
                // Act
                sut.buttonStateFlow.value.onClick()
                // Assert
                val newRounds = sut.timerStateFlow.value.laps
                assertThat(newRounds.size, `is`(previousRounds.size + 1))
                assertThat(newRounds.last().time, `is`(passedTime.millisToMinutesAndSeconds()))
                assertThat(newRounds.last().index, `is`(newRounds.lastIndex + 1))
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `18- given current round is not open timer and there is next round that does not start automatically, then add passed time from timer to laps in timerState`(passedTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    0,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                timerListenerSlot.captured.onTick(passedTime)
                val previousRounds = sut.timerStateFlow.value.laps
                // Act
                sut.buttonStateFlow.value.onClick()
                // Assert
                val newRounds = sut.timerStateFlow.value.laps
                assertThat(newRounds.size, `is`(previousRounds.size + 1))
                assertThat(newRounds.last().time, `is`(passedTime.millisToMinutesAndSeconds()))
                assertThat(newRounds.last().index, `is`(newRounds.lastIndex + 1))
            }

        @Test
        fun `19- given there is next round, then update currentRoundIndex and timerState title and create and start new timer for next Round`() =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                justRun { anyConstructed<CountUpTimerImpl>().removeListener(any()) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        0,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                timerListenerSlot.captured.onTick(passedTime)
                // mockk constructor again to cancel old constructor mock and enable new constructor
                mockkConstructor(CountUpTimerImpl::class)
                // Act
                sut.buttonStateFlow.value.onClick()

                // Assert
                assertThat(sut.timerStateFlow.value.type, `is`(nextRound.type))
                assertThat(expectedExercise.currenRoundIndex, `is`(initialIndex + 1))
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(nextRound.expectedTime),
                        EqMatcher(testDispatcher),
                    ).setListener(any())
                }
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(nextRound.expectedTime),
                        EqMatcher(testDispatcher),
                    ).start()
                }
            }

        @Test
        fun `20- given there is no next round, then show finished timerState and start button`() =
            runTest {
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                sut.buttonStateFlow.value.onClick()

                // Assert
                val timerState = sut.timerStateFlow.value
                assertThat(timerState.type, `is`(BreathingExercise.RoundType.FINISHED))
                assertThat(timerState.currentTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnStartText)))
            }

        @Test
        fun `21- given there is next round that is open timer, then show next button state`() =
            runTest {
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.HOLD,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                sut.buttonStateFlow.value.onClick()

                // Assert
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnNextText)))
            }

        @Test
        fun `22- given there is next round that is not open timer, then show pause button state`() =
            runTest {
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        300,
                        BreathingExercise.RoundType.EXHALE,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                sut.buttonStateFlow.value.onClick()

                // Assert
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnPauseText)))
            }
    }

    @Nested
    @DisplayName("When onTick invoked")
    inner class OnTick {
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `23- given current round has endTime and endTime is reached, then show next button state`(endTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        endTime,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                timerListenerSlot.captured.onTick(endTime)
                // Assert
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnNextText)))
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `24- given current round had endTime that is not reached yet, then do not change button`(endTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val timerTime = endTime - 1
                val currentRound =
                    BreathingExercise.BreathingRound(
                        endTime,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                every { anyConstructed<CountUpTimerImpl>().time } returns timerTime
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                sut.buttonStateFlow.test {
                    // catch current state
                    awaitItem()
                    // Act
                    timerListenerSlot.captured.onTick(timerTime)
                    // Assert
                    expectNoEvents()
                }
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `25- given current round is open timer, then do not change button`(passedTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                every { anyConstructed<CountUpTimerImpl>().time } returns passedTime
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                sut.buttonStateFlow.test {
                    // catch current state
                    awaitItem()
                    // Act
                    timerListenerSlot.captured.onTick(passedTime)
                    // Assert
                    expectNoEvents()
                }
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 5, 10, 200, 1000, 2000, 3000, 4000, 5000, 1000000, 1223123, Long.MAX_VALUE])
        fun `26- given current round is open timer, then set progress 0 and update currentTimeText and totalTimeText in timerState`(onTickTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val firstRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(firstRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                every { anyConstructed<CountUpTimerImpl>().time } returns onTickTime
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                timerListenerSlot.captured.onTick(onTickTime)
                // Assert
                val timerState = sut.timerStateFlow.value
                assertThat(timerState.progress, `is`(0F))
                assertThat(timerState.currentTimeText, `is`(onTickTime.millisToMinutesAndSeconds()))
                assertThat(timerState.totalTimeText, `is`(onTickTime.millisToMinutesAndSeconds()))
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 5, 10, 200, 1000, 2000, 3000, 4000, 5000, 1000000, 1223123, Long.MAX_VALUE])
        fun `27- given current round has endTime, then update progress, currentTimeText and totalTimeText in timerState`(onTickTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val firstRound =
                    BreathingExercise.BreathingRound(
                        100L,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(firstRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                every { anyConstructed<CountUpTimerImpl>().time } returns onTickTime
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // Act
                timerListenerSlot.captured.onTick(onTickTime)
                // Assert
                val timerState = sut.timerStateFlow.value
                assertThat(timerState.progress, `is`(onTickTime.toFloat() / firstRound.expectedTime))
                assertThat(timerState.currentTimeText, `is`(onTickTime.millisToMinutesAndSeconds()))
                assertThat(timerState.totalTimeText, `is`(onTickTime.millisToMinutesAndSeconds()))
            }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 6, 10])
        fun `28- given there were already rounds finished, then set totalTime current timer time to time of previous rounds`(roundCount: Int) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns roundCount.toLong()
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                val rounds = mutableListOf<BreathingExercise.BreathingRound>()
                for (i in 1..roundCount) {
                    rounds.add(
                        BreathingExercise.BreathingRound(
                            i.toLong() * 100,
                            BreathingExercise.RoundType.NORMAL_BREATHING,
                            true,
                        ),
                    )
                }
                // safe expected total time for all rounds except last one + time used for onTick
                val expectedTotalTime = rounds.sumOf { it.expectedTime } + roundCount
                rounds.add(
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        true,
                    ),
                )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = rounds
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()
                // simulate all rounds except last one to be done
                rounds.forEachIndexed { index, round ->
                    if (index < rounds.lastIndex) {
                        timerListenerSlot.captured.onFinish(round.expectedTime)
                    }
                }
                // Act
                timerListenerSlot.captured.onTick(roundCount.toLong())
                // Assert
                assertThat(sut.timerStateFlow.value.totalTimeText, `is`(expectedTotalTime.millisToMinutesAndSeconds()))
            }
    }

    @Nested
    @DisplayName("When onFinish invoked")
    inner class OnFinish {
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `29- given current round is open timer, then add passed time from timer to laps in timerState`(passedTime: Long) =
            runTest {
                // Arrange
                val listenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(listenerSlot)) }
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    passedTime,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer to get listener slot
                sut.buttonStateFlow.value.onClick()
                val previousRounds = sut.timerStateFlow.value.laps
                // Act
                listenerSlot.captured.onFinish(passedTime)
                // Assert
                val newRounds = sut.timerStateFlow.value.laps
                assertThat(newRounds.size, `is`(previousRounds.size + 1))
                assertThat(newRounds.last().time, `is`(passedTime.millisToMinutesAndSeconds()))
                assertThat(newRounds.last().index, `is`(newRounds.lastIndex + 1))
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 3, 4, 5, 10, 1223123, Long.MAX_VALUE])
        fun `30- given current round is not open timer and there is next round that does not start automatically, then add passed time from timer to laps in timerState`(passedTime: Long) =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> =
                            listOf(
                                BreathingExercise.BreathingRound(
                                    0,
                                    BreathingExercise.RoundType.NORMAL_BREATHING,
                                    false,
                                ),
                                BreathingExercise.BreathingRound(
                                    1000L,
                                    BreathingExercise.RoundType.LOWER_BREATHING,
                                    false,
                                ),
                            )
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer
                sut.buttonStateFlow.value.onClick()
                val previousRounds = sut.timerStateFlow.value.laps
                // Act
                timerListenerSlot.captured.onFinish(passedTime)
                // Assert
                val newRounds = sut.timerStateFlow.value.laps
                assertThat(newRounds.size, `is`(previousRounds.size + 1))
                assertThat(newRounds.last().time, `is`(passedTime.millisToMinutesAndSeconds()))
                assertThat(newRounds.last().index, `is`(newRounds.lastIndex + 1))
            }

        @Test
        fun `31- given there is next round, then update currentRoundIndex and timerState title and create and start new timer for next Round`() =
            runTest {
                // Arrange
                val timerListenerSlot = slot<CountUpTimer.Listener>()
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(timerListenerSlot)) }
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                justRun { anyConstructed<CountUpTimerImpl>().removeListener(any()) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        0,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        1000L,
                        BreathingExercise.RoundType.LOWER_BREATHING,
                        false,
                    )
                val initialIndex = 0
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = initialIndex
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer
                sut.buttonStateFlow.value.onClick()
                // mockk constructor again to cancel old constructor mock and enable new constructor
                mockkConstructor(CountUpTimerImpl::class)
                // Act
                timerListenerSlot.captured.onFinish(passedTime)

                // Assert
                assertThat(sut.timerStateFlow.value.type, `is`(nextRound.type))
                assertThat(expectedExercise.currenRoundIndex, `is`(initialIndex + 1))
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(nextRound.expectedTime),
                        EqMatcher(testDispatcher),
                    ).setListener(any())
                }
                verify {
                    constructedWith<CountUpTimerImpl>(
                        EqMatcher(0L),
                        EqMatcher(TimeUnit.SECONDS.toMillis(1)),
                        EqMatcher(nextRound.expectedTime),
                        EqMatcher(testDispatcher),
                    ).start()
                }
            }

        @Test
        fun `32- given there is no next round, then show finished timerState and start button`() =
            runTest {
                val passedTime = 200L
                val listenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(listenerSlot)) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer
                sut.buttonStateFlow.value.onClick()
                // Act
                listenerSlot.captured.onFinish(passedTime)

                // Assert
                val timerState = sut.timerStateFlow.value
                assertThat(timerState.type, `is`(BreathingExercise.RoundType.FINISHED))
                assertThat(timerState.currentTimeText, `is`(BreathingViewModel.STARTING_TIME_STRING))
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnStartText)))
            }

        @Test
        fun `33- given there is next round that is open timer, then show next button state`() =
            runTest {
                val listenerSlot = slot<CountUpTimer.Listener>()
                val passedTime = 200L
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(listenerSlot)) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.HOLD,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer
                sut.buttonStateFlow.value.onClick()
                // Act
                listenerSlot.captured.onFinish(passedTime)

                // Assert
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnNextText)))
            }

        @Test
        fun `34- given there is next round that is not open timer, then show pause button state`() =
            runTest {
                val passedTime = 200L
                val listenerSlot = slot<CountUpTimer.Listener>()
                mockkConstructor(CountUpTimerImpl::class)
                every { anyConstructed<CountUpTimerImpl>().stop() } returns passedTime
                justRun { anyConstructed<CountUpTimerImpl>().setListener(capture(listenerSlot)) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val nextRound =
                    BreathingExercise.BreathingRound(
                        300,
                        BreathingExercise.RoundType.EXHALE,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound, nextRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer
                sut.buttonStateFlow.value.onClick()
                // Act
                listenerSlot.captured.onFinish(passedTime)

                // Assert
                assertThat(sut.buttonStateFlow.value.text, `is`(Text.TextRes(R.string.btnPauseText)))
            }
    }

    @Nested
    @DisplayName("When onCleared invoked")
    inner class OnCleared {
        @Test
        fun `35- given there is timer, then call stop on timer and remove listeners`() =
            runTest {
                // Arrange
                mockkConstructor(CountUpTimerImpl::class)
                justRun { anyConstructed<CountUpTimerImpl>().start() }
                every { anyConstructed<CountUpTimerImpl>().stop() } returns 0L
                justRun { anyConstructed<CountUpTimerImpl>().removeListener(any()) }
                val currentRound =
                    BreathingExercise.BreathingRound(
                        BreathingExercise.OPEN_TIMER,
                        BreathingExercise.RoundType.NORMAL_BREATHING,
                        false,
                    )
                val expectedExercise =
                    object : BreathingExercise {
                        override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                        override val rounds: List<BreathingExercise.BreathingRound> = listOf(currentRound)
                        override var currenRoundIndex: Int = 0
                    }
                every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
                sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
                // start timer and simulate time passed to trigger update of button state to next button
                sut.buttonStateFlow.value.onClick()

                val viewModelStore = ViewModelStore()
                val viewModelProvider =
                    ViewModelProvider(
                        viewModelStore,
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return sut as T
                            }
                        },
                    )
                viewModelProvider.get(BreathingViewModel::class.java)

                // Act
                // To trigger onCleared call in ViewModel
                viewModelStore.clear()
                // Assert
                verify { anyConstructed<CountUpTimerImpl>().stop() }
                verify { anyConstructed<CountUpTimerImpl>().removeListener(any()) }
            }

        @Test
        fun `36- given there is no timer, then nothing happens`() =
            runTest {
                // Arrange
                val viewModelStore = ViewModelStore()
                val viewModelProvider =
                    ViewModelProvider(
                        viewModelStore,
                        object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return sut as T
                            }
                        },
                    )
                viewModelProvider.get(BreathingViewModel::class.java)

                // Act
                // To trigger onCleared call in ViewModel
                viewModelStore.clear()
                // Assert
                assertThat(true, `is`(true))
            }
    }

    @Test
    fun `37- When pause button state clicked, then call pause on timer and show resume button state`() =
        runTest {
            // Arrange
            mockkConstructor(CountUpTimerImpl::class)
            justRun { anyConstructed<CountUpTimerImpl>().pause() }
            val expectedExercise =
                object : BreathingExercise {
                    override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                    override val rounds: List<BreathingExercise.BreathingRound> =
                        listOf(
                            BreathingExercise.BreathingRound(
                                1000L,
                                BreathingExercise.RoundType.NORMAL_BREATHING,
                                false,
                            ),
                            BreathingExercise.BreathingRound(
                                1000L,
                                BreathingExercise.RoundType.LOWER_BREATHING,
                                true,
                            ),
                        )
                    override var currenRoundIndex: Int = 0
                }
            every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
            sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
            // trigger pause button shown
            sut.buttonStateFlow.value.onClick.invoke()
            sut.buttonStateFlow.test {
                // catch initial state
                awaitItem()
                // Act
                sut.buttonStateFlow.value.onClick.invoke()
                // Assert
                assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnResumeText)))
                verify { anyConstructed<CountUpTimerImpl>().pause() }
            }
        }

    @Test
    fun `38- When resume button state clicked, then call resume on timer and show pause button state`() =
        runTest {
            // Arrange
            mockkConstructor(CountUpTimerImpl::class)
            justRun { anyConstructed<CountUpTimerImpl>().pause() }
            justRun { anyConstructed<CountUpTimerImpl>().resume() }
            val expectedExercise =
                object : BreathingExercise {
                    override val title: Text = Text.TextRes(R.string.buteyko_breathing_title)
                    override val rounds: List<BreathingExercise.BreathingRound> =
                        listOf(
                            BreathingExercise.BreathingRound(
                                1000L,
                                BreathingExercise.RoundType.NORMAL_BREATHING,
                                false,
                            ),
                            BreathingExercise.BreathingRound(
                                1000L,
                                BreathingExercise.RoundType.LOWER_BREATHING,
                                true,
                            ),
                        )
                    override var currenRoundIndex: Int = 0
                }
            every { getCurrentBreathingExerciseUseCase() } returns expectedExercise
            sut = BreathingViewModel(getCurrentBreathingExerciseUseCase, testDispatcher)
            // trigger pause button shown
            sut.buttonStateFlow.value.onClick.invoke()
            // pause to trigger resume button shown
            sut.buttonStateFlow.value.onClick.invoke()
            sut.buttonStateFlow.test {
                // catch initial state
                awaitItem()
                // Act
                sut.buttonStateFlow.value.onClick.invoke()
                // Assert
                assertThat(awaitItem().text, `is`(Text.TextRes(R.string.btnPauseText)))
                verify { anyConstructed<CountUpTimerImpl>().resume() }
            }
        }
}
