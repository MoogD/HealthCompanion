package com.dom.healthcompanion.domain.breathing.usecase

import com.dom.androidUtils.time.TimeHelper
import com.dom.healthcompanion.data.BreathingSummaryDataSource
import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import com.dom.testUtils.TestDispatcherProvider
import io.mockk.clearAllMocks
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.stream.Stream
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

class SaveBreathingDataUseCaseTest {
    // region test cases

    // 1- When invoked, then save breathing data with current timestamp to data source

    // endregion

    private val testDispatcher = spyk<TestDispatcherProvider>()
    private val dataSource = mockk<BreathingSummaryDataSource>()
    private val timeHelper = mockk<TimeHelper>()

    private lateinit var sut: SaveBreathingDataUseCase

    @BeforeEach
    fun setUp() {
        sut = SaveBreathingDataUseCase(dataSource, testDispatcher, timeHelper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ArgumentsSource(BreathingSummaryAndTimestampArgumentsProvider::class)
    fun `1- When invoked, then save breathing data with current timestamp to data source`(
        breathingSummary: BreathingSummary,
        timestamp: Long,
    ) =
        runBlocking {
            // Arrange
            every { timeHelper.getCurrentTimeMillis() } returns timestamp
            val expectedData = BreathingDataEntity.fromBreathingSummary(breathingSummary, timestamp)
            coJustRun { dataSource.saveBreathingData(expectedData) }
            // Act
            sut(breathingSummary)
            // Assert
            coVerify { dataSource.saveBreathingData(expectedData) }
            verify { testDispatcher.io }
        }

    private class BreathingSummaryAndTimestampArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments>? {
            return Stream.of(
                Arguments.of(
                    BreathingSummary(
                        "Test",
                        emptyList(),
                    ),
                    104232323423L,
                ),
                Arguments.of(
                    BreathingSummary(
                        "Buteyko",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.INHALE,
                                1000L,
                                2000L,
                            ),
                        ),
                    ),
                    204232323423L,
                ),
                Arguments.of(
                    BreathingSummary(
                        "asfasdfs",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.EXHALE,
                                2000L,
                                1000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                -1,
                                1000L,
                            ),
                        ),
                    ),
                    9042323423L,
                ),
                Arguments.of(
                    BreathingSummary(
                        "BoxBreathing",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.INHALE,
                                2000L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                2000L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.EXHALE,
                                2000L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                2000L,
                                2000L,
                            ),
                        ),
                    ),
                    1243210431234533,
                ),
                Arguments.of(
                    BreathingSummary(
                        "New test",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.LOWER_BREATHING,
                                123412431243,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                1000L,
                                12314,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.LOWER_BREATHING,
                                10L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                1000L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.LOWER_BREATHING,
                                1000L,
                                2000L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                523425,
                                141234,
                            ),
                        ),
                    ),
                    91287432143L,
                ),
            )
        }
    }
}
