package com.dom.healthcompanion.data

import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity
import com.dom.healthcompanion.data.database.breathing.BreathingSummaryDao
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import io.mockk.clearAllMocks
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import java.util.stream.Stream
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

class BreathingSummaryLocalDataSourceTest {
    // region test cases

    // 1- When saveBreathingData invoked, then call insert on dao

    // endregion

    private val breathingSummaryDao = mockk<BreathingSummaryDao>()
    private lateinit var sut: BreathingSummaryLocalDataSource

    @BeforeEach
    fun setUp() {
        sut = BreathingSummaryLocalDataSource(breathingSummaryDao)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ArgumentsSource(BreathingDataEntityArgumentsProvider::class)
    fun `1- When saveBreathingData invoked, then call insert on dao`(breathingData: BreathingDataEntity) =
        runTest {
            // Arrange
            justRun { breathingSummaryDao.insert(breathingData) }
            // Act
            sut.saveBreathingData(breathingData)
            // Assert
            verify { breathingSummaryDao.insert(breathingData) }
        }

    private class BreathingDataEntityArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    BreathingDataEntity(
                        0,
                        123123,
                        "test",
                        emptyList(),
                    ),
                ),
                Arguments.of(
                    BreathingDataEntity(
                        1,
                        1231,
                        "Buteyko",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                -1,
                                10,
                            ),
                        ),
                    ),
                ),
                Arguments.of(
                    BreathingDataEntity(
                        2,
                        13,
                        "test12341",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.INHALE,
                                100,
                                100,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.EXHALE,
                                1,
                                10,
                            ),
                        ),
                    ),
                ),
                Arguments.of(
                    BreathingDataEntity(
                        3,
                        345,
                        "new",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                10,
                                10,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                100,
                                10,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.LOWER_BREATHING,
                                96,
                                1000,
                            ),
                        ),
                    ),
                ),
                Arguments.of(
                    BreathingDataEntity(
                        4,
                        43234623642,
                        "blabla",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                10,
                                10,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                0,
                                0,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.NORMAL_BREATHING,
                                96,
                                1000,
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
