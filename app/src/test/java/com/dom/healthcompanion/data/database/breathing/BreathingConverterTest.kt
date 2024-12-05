package com.dom.healthcompanion.data.database.breathing

import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BreathingConverterTest {
    // region test cases

    // 1- When listToJson is called, then return Gson toJson result
    // 2- When jsonToList is called, then return Gson fromJson result

    // endregion

    private lateinit var sut: BreathingConverter
    private val listType = object : TypeToken<List<BreathingSummary.BreathingRoundSummary>>() {}.type

    @BeforeEach
    fun setUp() {
        mockkConstructor(Gson::class)
        sut = BreathingConverter()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `1- When listToJson is called, then return Gson toJson result`() {
        // Arrange
        val summaries = listOf<BreathingSummary.BreathingRoundSummary>(mockk(), mockk(), mockk(), mockk())
        val expectedResult = "correctResult"
        every { anyConstructed<Gson>().toJson(summaries) } returns expectedResult
        // Act
        val result = sut.listToJson(summaries)
        // Assert
        assertThat(result, `is`(expectedResult))
    }

    @Test
    fun `2- When jsonToList is called, then return Gson fromJson result`() {
        // Arrange
        val jsonString = "BreathingSummary.BreathingRoundSummary@123123"
        val expectedResult = listOf<BreathingSummary.BreathingRoundSummary>(mockk(), mockk(), mockk(), mockk())
        every { anyConstructed<Gson>().fromJson<List<BreathingSummary.BreathingRoundSummary>>(jsonString, listType) } returns expectedResult
        // Act
        val result = sut.jsonToList(jsonString)
        // Assert
        assertThat(result, `is`(expectedResult))
    }
}
