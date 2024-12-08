package com.dom.healthcompanion.data.database.breathing

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dom.healthcompanion.data.database.AppDatabase
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BreathingSummaryDaoTest {
    // region test cases

    // 1- When getAll is invoked, given there are entries in db, then return all entities from database
    // 2- When getAll is invoked, given there are no entries in db, then return empty list

    // 3- When insert invoked, given there is no entry for primary key, then insert new entry
    // 4- When insert invoked, given there is an entry for primary key, then replace entry

    // endregion

    private lateinit var sut: BreathingSummaryDao
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val converter = BreathingConverter()
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java,
            ).addTypeConverter(converter).build()
        sut = db.breathingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `1- When getAll is invoked, given there are entries in db, then return all entities from database`() =
        runTest {
            withContext(Dispatchers.IO) {
                // Arrange
                val expectedResult = getBreathingDataEntries()
                for (entity in expectedResult) {
                    sut.insert(entity)
                }
                // Act
                val result = sut.getAll()
                // Assert
                assertThat(result.size, `is`(expectedResult.size))
                for (resultEntry in result) {
                    assertThat(expectedResult.contains(resultEntry), `is`(true))
                }
            }
        }

    @Test
    fun `2- When getAll is invoked, given there are no entries in db, then return empty list`() =
        runTest {
            withContext(Dispatchers.IO) {
                // Act
                val result = sut.getAll()
                // Assert
                assertThat(result.size, `is`(0))
            }
        }

    @Test
    fun `3- When insert invoked, given there is no entry for primary key, then insert new entry`() =
        runTest {
            withContext(Dispatchers.IO) {
                // Arrange
                val newEntry =
                    BreathingDataEntity(
                        1,
                        1234124L,
                        "Test",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                112431L,
                                121424312L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.INHALE,
                                112431L,
                                121424312L,
                            ),
                        ),
                    )
                // Act
                sut.insert(newEntry)
                // Assert
                val result = sut.getAll()
                assertThat(result.size, `is`(1))
                assertThat(result[0], `is`(newEntry))
            }
        }

    @Test
    fun `4- When insert invoked, given there is an entry for primary key, then replace entry`() =
        runTest {
            withContext(Dispatchers.IO) {
                // Arrange
                val oldEntry =
                    BreathingDataEntity(
                        1,
                        1234124L,
                        "Test",
                        listOf(
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.HOLD,
                                112431L,
                                121424312L,
                            ),
                            BreathingSummary.BreathingRoundSummary(
                                BreathingSummary.RoundType.INHALE,
                                112431L,
                                121424312L,
                            ),
                        ),
                    )
                sut.insert(oldEntry)
                val newEntry =
                    BreathingDataEntity(
                        1,
                        1,
                        "New",
                        emptyList(),
                    )
                // Act
                sut.insert(newEntry)
                // Assert
                val result = sut.getAll()
                assertThat(result.size, `is`(1))
                assertThat(result[0], `is`(newEntry))
            }
        }

    // region helper functions

    private fun getBreathingDataEntries() =
        listOf(
            BreathingDataEntity(
                1,
                1234124L,
                "Test",
                listOf(
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.HOLD,
                        112431L,
                        121424312L,
                    ),
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.INHALE,
                        112431L,
                        121424312L,
                    ),
                ),
            ),
            BreathingDataEntity(
                2,
                9234124L,
                "Test",
                listOf(
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.EXHALE,
                        1L,
                        1L,
                    ),
                ),
            ),
            BreathingDataEntity(3, 12434234124L, "Test", emptyList()),
            BreathingDataEntity(
                4,
                4124L,
                "Test",
                listOf(
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.LOWER_BREATHING,
                        112431L,
                        121424312L,
                    ),
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.INHALE,
                        112431L,
                        9999124313L,
                    ),
                    BreathingSummary.BreathingRoundSummary(
                        BreathingSummary.RoundType.NORMAL_BREATHING,
                        124321431243,
                        121421234124312L,
                    ),
                ),
            ),
        )
    // endregion
}
