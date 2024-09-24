package com.dom.timer

import com.dom.testUtils.TestDispatcherProvider
import com.dom.utils.DispatchersProvider
import com.dom.utils.DispatchersProviderImpl
import io.mockk.clearAllMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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

class CountUpTimerTest {
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
    // 10- When start is called, given endTimeInMillis 0 provided, then do not call onTick and stop timer
    // 11- When start is called, given endTimeInMillis > 0 provided, then start timer and stop after endTimeInMillis reached
    // 12- When start is called, given endTimeInMillis < 0 provided, then start timer
    // 13- When start is called, given endTimeInMillis not provided, then start timer
    // 14- When start is called, given endTimeInMillis and startTimeInMillis provided with endTimeInMillis <= startTimeInMillis, then do not start timer
    // 15- When start is called, given startTimeInMillis >= 0 provided and no endTimeInMillis provided, then start timer beginning with startTimeInMillis
    // 16- When start is called, given startTimeInMillis > 0 provided and endTimeInMillis provided with endTimeInMillis > startTimeInMilli, then start timer beginning with startTimeInMillis
    // 17- When start is called, given endTimeInMillis and onFinish provided, then call onFinish after timer is stopped
    // 18- When start is called, given onFinish but no endTimeInMillis provided, then don't call onFinish

    // 19- When timer is active, then time variable is updated
    // 20- When timer is active, then onTick is called with correct time variable once every periodInMillis passed

    // endregion

    private val testDispatcherProvider = TestDispatcherProvider()

    private lateinit var sut: CountUpTimer

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
                CountUpTimer(
                    onTick = { _: Long -> },
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
                CountUpTimer(
                    onTick = { _: Long -> },
                    startTimeInMillis = startTimeInMillis,
                    dispatchersProvider = testDispatcherProvider,
                )
            // Assert
            assertThat(sut.time, `is`(0))
        }

        @Test
        fun `3- given there is no startTimeInMillis provided, then set time to 0`() {
            // Act
            sut = CountUpTimer(onTick = { _: Long -> }, dispatchersProvider = testDispatcherProvider)
            // Assert
            assertThat(sut.time, `is`(0))
        }

        @Test
        fun `4- given no dispatchersProvider provided, then use DispatchersProviderImpl`() {
            // Arrange
            mockkConstructor(DispatchersProviderImpl::class)
            every { anyConstructed<DispatchersProviderImpl>().main } returns testDispatcherProvider.main
            // Act
            sut = CountUpTimer(onTick = { _: Long -> })
            verify { anyConstructed<DispatchersProviderImpl>().main }
        }

        @Test
        fun `5- given dispatchersProvider provided, then use provided dispatchersProvider`() {
            // Arrange
            val provider = mockk<DispatchersProvider>()
            every { provider.main } returns testDispatcherProvider.main
            // Act
            sut = CountUpTimer(onTick = { _: Long -> }, dispatchersProvider = provider)
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
                sut =
                    CountUpTimer(
                        onTick = { _: Long -> },
                        dispatchersProvider = testDispatcherProvider,
                    )
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
                    CountUpTimer(
                        onTick = { _: Long -> },
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
                    CountUpTimer(
                        onTick = { _: Long -> },
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
                sut =
                    CountUpTimer(
                        onTick = { _: Long -> },
                        dispatchersProvider = testDispatcherProvider,
                    )
                mockkStatic("kotlinx.coroutines.DelayKt")
                // Act
                sut.start()
                // Assert
                coVerify { delay(CountUpTimer.DEFAULT_PERIOD) }
            }

        @Test
        fun `10- given endTimeInMillis 0 provided, then do not call onTick and stop timer`() =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // needs to be mocked so it wont be skipped in test scope
                mockkStatic("kotlinx.coroutines.DelayKt")
                coJustRun { delay(any<Long>()) }
                sut =
                    CountUpTimer(
                        onTick = onTick,
                        endTimeInMillis = 0,
                        dispatchersProvider = testDispatcherProvider,
                    )

                // Act
                sut.start()
                // Assert
                coVerify(exactly = 0) { onTick(any()) }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 5, 10, 1213])
        fun `11- given endTimeInMillis bigger 0 provided, then start timer and stop after endTimeInMillis reached`(endTimeInMillis: Long) =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1L,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                advanceTimeBy(2 * endTimeInMillis)
                // Assert
                coVerify { onTick(or(less(endTimeInMillis), eq(endTimeInMillis))) }
                coVerify(exactly = 0) { onTick(more(endTimeInMillis)) }
                // check that timer task is cancelled
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [-1, -2, -5, -10, -1213])
        fun `12- given endTimeInMillis smaller 0 provided, then start timer`(endTimeInMillis: Long) =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(1000)
                coVerify { onTick(any<Long>()) }
                // check that timer task is still active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(false))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `13- given endTimeInMillis not provided, then start timer`() =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(1000)
                coVerify { onTick(any<Long>()) }
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
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTimeInMillis)
                coVerify(exactly = 0) { onTick(any<Long>()) }
                // check that timer task is not active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @ParameterizedTest
        @ValueSource(longs = [1, 2, 10, 1213])
        fun `15- given startTimeInMillis bigger or equal 0 provided and no endTimeInMillis provided, then start timer beginning with startTimeInMillis`(startTimeInMillis: Long) =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(5 * startTimeInMillis)
                coVerify(exactly = 0) { onTick(or(less(startTimeInMillis), eq(startTimeInMillis))) }
                coVerify { onTick(more(startTimeInMillis)) }
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
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 1,
                        startTimeInMillis = startTimeInMillis,
                        endTimeInMillis = endTimeInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTimeInMillis + 10)
                coVerify(exactly = 0) { onTick(or(less(startTimeInMillis), eq(startTimeInMillis))) }
                coVerify { onTick(more(startTimeInMillis)) }
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `17- given endTimeInMillis and onFinish provided, then call onFinish after timer is stopped`() =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                val onFinish: () -> Unit = mockk(relaxed = true)
                val endTime = 1000L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 100,
                        endTimeInMillis = endTime,
                        onFinish = onFinish,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(endTime + 10)
                coVerify(exactly = 1) { onFinish() }
                // check that timer task is not active
                assertThat(backgroundScope.coroutineContext.job.children.all { it.isCancelled }, `is`(true))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `18- given onFinish but no endTimeInMillis provided, then dont call onFinish`() =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                val onFinish: () -> Unit = mockk(relaxed = true)
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = 100,
                        dispatchersProvider = testDispatcherProvider,
                        onFinish = onFinish,
                    )
                // Act
                sut.start()
                // Assert
                advanceTimeBy(10000)
                coVerify(exactly = 0) { onFinish() }
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
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                val periodInMillis = 100L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
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
        fun `20- then onTick is called with correct time variable once every periodInMillis passed`(rounds: Int) =
            runTest {
                // Arrange
                val onTick: (Long) -> Unit = mockk(relaxed = true)
                val periodInMillis = 100L
                // mock scope to run timer job in background of test coroutine scope
                mockkStatic(::CoroutineScope)
                every { CoroutineScope(any()) } returns backgroundScope

                sut =
                    CountUpTimer(
                        onTick = onTick,
                        periodInMillis = periodInMillis,
                        dispatchersProvider = testDispatcherProvider,
                    )
                // Act
                sut.start()
                // Assert
                for (i in 1..rounds) {
                    advanceTimeBy(periodInMillis + 2)
                    verify { onTick(periodInMillis * i) }
                }
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
