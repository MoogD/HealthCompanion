package com.dom.androidUtils.time

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.Calendar
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TimeHelperImplTest {
    // region test cases

    // 1- When getCurrentTimeMillis invoked, then return current time in millis

    // endregion

    private lateinit var sut: TimeHelperImpl

    @BeforeEach
    fun setUp() {
        sut = TimeHelperImpl()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ValueSource(longs = [104232323423L, 204232323423L, 9042323423L, 1243210431234533L, Long.MAX_VALUE])
    fun `1- When getCurrentTimeMillis invoked, then return current time in millis`(currentTimeMillis: Long) {
        // Arrange
        val mockCalendar = mockk<Calendar>()
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns mockCalendar
        every { mockCalendar.timeInMillis } returns currentTimeMillis
        // Act
        val result = sut.getCurrentTimeMillis()
        // Assert
        assertThat(result, `is`(currentTimeMillis))
    }
}
