package com.dom.timer

import com.dom.testUtils.TestDispatcherProvider
import com.dom.utils.DispatchersProvider
import com.dom.utils.DispatchersProviderImpl
import io.mockk.clearAllMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import java.util.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource

class CountUpTimerImplTest {
    // region test cases

    // 1- When initialized, given there is startTimeInMillis >= 0 provided, then set time to startTimeInMillis
    // 2- When initialized, given there is startTimeInMillis < 0 provided, then set time to 0
    // 3- When initialized, given there is no startTimeInMillis provided, then set time to 0
    // 4- When initialized, given no dispatchersProvider provided, then use DispatchersProviderImpl
    // 5- When initialized, given dispatchersProvider provided, then use provided dispatchersProvider

    // 6- When start is called, then cancel existing timers
    // 7- When start is called, given periodInMillis > 0 provided, then use periodInMillis for delay
    // 8- When start is called, given periodInMillis <= 0 provided, then use DEFAULT_PERIOD for delay
    // 9- When start is called, given periodInMillis not provided, then use DEFAULT_PERIOD for delay
    // 10- When start is called, given endTimeInMillis 0 provided and listener is set, then do not call onTick and stop timer
    // 11- When start is called, given endTimeInMillis > 0 provided, then start timer and stop after endTimeInMillis reached
    // 12- When start is called, given endTimeInMillis < 0 provided, then start timer
    // 13- When start is called, given endTimeInMillis not provided, then start timer
    // 14- When start is called, given endTimeInMillis and startTimeInMillis provided with endTimeInMillis <= startTimeInMillis, then do not start timer
    // 15- When start is called, given startTimeInMillis >= 0 provided and no endTimeInMillis provided, then start timer beginning with startTimeInMillis
    // 16- When start is called, given startTimeInMillis > 0 provided and endTimeInMillis provided with endTimeInMillis > startTimeInMilli, then start timer beginning with startTimeInMillis
    // 17- When start is called, given endTimeInMillis and listener is set provided, then call onFinish after timer is stopped
    // 18- When start is called, given listener is set but no endTimeInMillis provided, then don't call onFinish

    // 19- When timer is active, then time variable is updated
    // 20- When timer is active, given listener was set, then onTick is called with correct time variable once every periodInMillis passed
    // 21- When timer is active, given endTime is reached and no listener is set, then just end timer

    // 22- When setListener is called, then provided listener is notified about future events
    // 23- When setListener is called, given there was already a listener set, then the old listener is not notified about future events

    // 24- When removeListener is called, given there is no listener, then nothing happens
    // 25- When removeListener is called, given there was a listener, then listener is not notified about future events

    // endregion

    private val testDispatcherProvider = TestDispatcherProvider()
    private val mockListener = mockk<CountUpTimer.Listener>(relaxed = true)

    private lateinit var sut: CountUpTimerImpl

    @AfterEach
    fun tearDown() {
        sut.stop()
        clearAllMocks()
    }

    @Nested
    @DisplayName("When initialized")
    inner class Initialized {
        @ParameterizedTest
        @ValueSource(longs = [0, 1, 2, 10, 1213, 32151251, Long.MAX_VALUE])
        fun `1- given there is startTimeInMillis bigger or equal 0 provided, then set time to startTimeInMillis`(startTimeInMillis: Long) {
            // Act
            sut =
                CountUpTimerImpl(
                    startTimeInMillis = startTimeInMillis,
                    dispatchersProvider = testDispatcherProvider,
                )
            // Assert
            assertThat(sut.time, `is`(startTimeInMillis))
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, -2, -10, -1213, -32151251, Long.MIN_VALUE])
        fun `2- given there is startTimeInMillis smaller 0 provided, then set time to 0`(startTimeInMillis: Long) {
            // Act
            sut =
                CountUpTimerImpl(
                    startTimeInMillis = startTimeInMillis,
                    dispatchersProvider = testDispatcherProvider,
                )
            // Assert
            assertThat(sut.time, `is`(0))
        }

        @Test
        fun `3- given there is no startTimeInMillis provided, then set time to 0`() {
            // Act
            sut = CountUpTimerImpl(dispatchersProvider = testDispatcherProvider)
            // Assert
            assertThat(sut.time, `is`(0))
        }

        @Test
        fun `4- given no dispatchersProvider provided, then use DispatchersProviderImpl`() {
            // Arrange
            mockkObject(DispatchersProviderImpl)
            every { DispatchersProviderImpl.main } returns testDispatcherProvider.main
            // Act
            sut = CountUpTimerImpl()
            verify { DispatchersProviderImpl.main }
        }

        @Test
        fun `5- given dispatchersProvider provided, then use provided dispatchersProvider`() {
            // Arrange
            val provider = mockk<DispatchersProvider>()
            every { provider.main } returns testDispatcherProvider.main
            // Act
            sut = CountUpTimerImpl(dispatchersProvider = provider)
            verify { provider.main }
        }
    }

    @Nested
    @DisplayName("When start is called")
    inner class Start {
        @Test
        fun `6- then cancel existing timers`() =
            runTest {
                // Arrange
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope
                sut = CountUpTimerImpl(dispatchersProvider = testDispatcherProvider)
                // start endless background task
                val job =
                    backgroundScope.launch {
                        while (true) {
                            // no action needed
                        }
                    }
                // Act
                sut.start()
                // Assert
                // check that background task is cancelled
                assertThat(job.isCancelled, `is`(true))
            }

        @ParameterizedTest
        @ValueSource(longs = [1, 2, 10, 1213, 32151251])
        fun `7- given periodInMillis bigger 0 provided, then use periodInMillis for delay`(periodInMillis: Long) =
            runTest {
                // Arrange
                sut =
                    CountUpTimerImpl(
                        periodInMillis = periodInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                mockkStatic("kotlinx.coroutines.DelayKt")
                // Act
                sut.start()
                // Assert
                coVerify { delay(periodInMillis) }
            }

        @ParameterizedTest
        @ValueSource(longs = [0, -1, -2, -10, -1213, -32151251])
        fun `8- given periodInMillis smaller or equal 0 provided, then use DEFAULT_PERIOD for delay`(periodInMillis: Long) =
            runTest {
                // Arrange
                sut =
                    CountUpTimerImpl(
                        periodInMillis = periodInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                mockkStatic("kotlinx.coroutines.DelayKt")
                // Act
                sut.start()
                // Assert
                coVerify { delay(CountUpTimer.DEFAULT_PERIOD) }
            }

        @Test
        fun `9- given periodInMillis not provided, then use DEFAULT_PERIOD for delay`() =
            runTest {
                // Arrange
                sut = CountUpTimerImpl(dispatchersProvider = testDispatcherProvider)
                mockkStatic("kotlinx.coroutines.DelayKt")
                // Act
                sut.start()
                // Assert
                coVerify { delay(CountUpTimer.DEFAULT_PERIOD) }
            }

        @Test
        fun `10- given endTimeInMillis 0 provided and listener is set, then do not call onTick and stop timer`() =
            runTest {
                // Arrange
                // needs to be mocked so it wont be skipped in test scope
                mockkStatic("kotlinx.coroutines.DelayKt")
                coJustRun { delay(any<Long>()) }
                sut =
                    CountUpTimerImpl(
                        endTimeInMillis = 0,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                coVerify(exactly = 0) { mockListener.onTick(any()) }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 5, 10, 1213])
        fun `11- given endTimeInMillis bigger 0 provided, then start timer and stop after endTimeInMillis reached`(endTimeInMillis: Long) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1L,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                advanceTimeBy(2 * endTimeInMillis)
                // Assert
                coVerify { mockListener.onTick(or(less(endTimeInMillis), eq(endTimeInMillis))) }
                coVerify(exactly = 0) { mockListener.onTick(more(endTimeInMillis)) }
                // check that timer task is cancelled
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [-1, -2, -5, -10, -1213])
        fun `12- given endTimeInMillis smaller 0 provided, then start timer`(endTimeInMillis: Long) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(1000)
                coVerify { mockListener.onTick(any<Long>()) }
                // check that timer task is still active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(false))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `13- given endTimeInMillis not provided, then start timer`() =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(1000)
                coVerify { mockListener.onTick(any<Long>()) }
                // check that timer task is still active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(false))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ArgumentsSource(SmallerFirstOrEqualPairOfLongArgumentsProvider::class)
        fun `14- given endTimeInMillis and startTimeInMillis provided with endTimeInMillis smaller or equal startTimeInMillis, then do not start timer`(
            endTimeInMillis: Long,
            startTimeInMillis: Long,
        ) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTimeInMillis)
                coVerify(exactly = 0) { mockListener.onTick(any<Long>()) }
                // check that timer task is not active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 10, 1213])
        fun `15- given startTimeInMillis bigger or equal 0 provided and no endTimeInMillis provided, then start timer beginning with startTimeInMillis`(startTimeInMillis: Long) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(5 * startTimeInMillis)
                coVerify(exactly = 0) { mockListener.onTick(or(less(startTimeInMillis), eq(startTimeInMillis))) }
                coVerify { mockListener.onTick(more(startTimeInMillis)) }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ArgumentsSource(SmallerFirstPairOfLongArgumentsProvider::class)
        fun `16- given startTimeInMillis bigger 0 provided and endTimeInMillis provided with endTimeInMillis bigger startTimeInMillis, then start timer beginning with startTimeInMillis`(
            startTimeInMillis: Long,
            endTimeInMillis: Long,
        ) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTimeInMillis + 10)
                coVerify(exactly = 0) { mockListener.onTick(or(less(startTimeInMillis), eq(startTimeInMillis))) }
                coVerify { mockListener.onTick(more(startTimeInMillis)) }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `17- given endTimeInMillis provided and listener is set, then call onFinish after timer is stopped`() =
            runTest {
                // Arrange
                val endTime = 1000L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 100,
                        endTimeInMillis = endTime,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTime + 10)
                coVerify(exactly = 1) { mockListener.onFinish() }
                // check that timer task is not active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `18- given listener is set but no endTimeInMillis provided, then dont call onFinish`() =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = 100,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                advanceTimeBy(10000)
                coVerify(exactly = 0) { mockListener.onFinish() }
                // check that timer task is still active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(false))
            }
    }

    @Nested
    @DisplayName("When timer is active")
    inner class Active {
        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 5, 10])
        fun `19- then time variable is updated`(rounds: Int) =
            runTest {
                // Arrange
                val periodInMillis = 100L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = periodInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                for (i in 1..rounds) {
                    advanceTimeBy(periodInMillis + 2)
                    assertThat(sut.time, `is`(periodInMillis * i))
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 5, 10])
        fun `20- given listener was set, then onTick is called with correct time variable once every periodInMillis passed`(rounds: Int) =
            runTest {
                // Arrange
                val periodInMillis = 100L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        periodInMillis = periodInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                sut.setListener(mockListener)
                // Act
                sut.start()
                // Assert
                for (i in 1..rounds) {
                    advanceTimeBy(periodInMillis + 2)
                    verify { mockListener.onTick(periodInMillis * i) }
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `21- given endTime is reached and no listener is set, then just end timer`() =
            runTest {
                // Arrange
                val endTimeInMillis = 2 * CountUpTimer.DEFAULT_PERIOD
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimerImpl(
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(2 * endTimeInMillis)
                assertThat(sut.time, `is`(endTimeInMillis))
                // check timer not running
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }
    }

    @Nested
    @DisplayName("When setListener is called")
    inner class SetListener {
        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(ints = [2, 3, 4, 5, 10])
        fun `22- then provided listener is notified about future events`(rounds: Int) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope
                val endTime = CountUpTimer.DEFAULT_PERIOD * rounds
                sut = CountUpTimerImpl(endTimeInMillis = endTime, dispatchersProvider = testDispatcherProvider)
                // Act
                sut.setListener(mockListener)
                // Assert
                // trigger events
                sut.start()
                // Assert
                for (i in 1..rounds) {
                    advanceTimeBy(CountUpTimer.DEFAULT_PERIOD + 2)
                    verify { mockListener.onTick(CountUpTimer.DEFAULT_PERIOD * i) }
                }
                advanceTimeBy(CountUpTimer.DEFAULT_PERIOD + 2)
                coVerify { mockListener.onFinish() }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(ints = [2, 3, 4, 5, 10])
        fun `23- given there was already a listener set, then the old listener is not notified about future events`(rounds: Int) =
            runTest {
                // Arrange
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope
                val endTime = CountUpTimer.DEFAULT_PERIOD * rounds
                sut = CountUpTimerImpl(endTimeInMillis = endTime, dispatchersProvider = testDispatcherProvider)
                val oldListener = mockk<CountUpTimer.Listener>()
                sut.setListener(oldListener)
                // Act
                sut.setListener(mockListener)
                // Assert
                // trigger events
                sut.start()
                // Assert
                for (i in 1..rounds) {
                    advanceTimeBy(CountUpTimer.DEFAULT_PERIOD + 2)
                    verify { mockListener.onTick(CountUpTimer.DEFAULT_PERIOD * i) }
                }
                advanceTimeBy(CountUpTimer.DEFAULT_PERIOD + 2)
                coVerify { mockListener.onFinish() }
                coVerify(exactly = 0) { oldListener.onTick(any()) }
                coVerify(exactly = 0) { oldListener.onFinish() }
            }
    }

    @Nested
    @DisplayName("When removeListener is called")
    inner class RemoveListener {
        @Test
        fun `24- given there is no listener, then nothing happens`() {
            // Arrange
            sut = CountUpTimerImpl()
            // Act
            sut.removeListener(mockListener)
            // Assert
            assertThat(true, `is`(true))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `25- given there was a listener, then listener is not notified about future events`() =
            runTest {
                // Arrange
                val endTime = 1000L
                sut = CountUpTimerImpl(endTimeInMillis = endTime, dispatchersProvider = testDispatcherProvider)
                sut.setListener(mockListener)
                // Act
                sut.removeListener(mockListener)
                // Assert
                // trigger events
                sut.start()
                advanceTimeBy(2 * endTime)
                verify(exactly = 0) { mockListener.onTick(any()) }
                verify(exactly = 0) { mockListener.onFinish() }
            }
    }

    private class SmallerFirstOrEqualPairOfLongArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.arguments(12L, 12L),
                Arguments.arguments(10L, 12L),
                Arguments.arguments(1000L, 1000L),
                Arguments.arguments(1L, 12L),
                Arguments.arguments(20L, 30L),
                Arguments.arguments(12L, 1122L),
            )
        }
    }

    private class SmallerFirstPairOfLongArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.arguments(10L, 12L),
                Arguments.arguments(1L, 12L),
                Arguments.arguments(20L, 30L),
                Arguments.arguments(12L, 1122L),
            )
        }
    }
}
