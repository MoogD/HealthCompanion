package com.dom.healthcompanion.domain.breathing.usecase

import com.dom.healthcompanion.domain.breathing.model.ButeykoBreathing
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCurrentBreathingExerciseUseCaseTest {
    // region test cases

    // 1- When invoked, then return ButeykoBreathing

    // endregion

    private lateinit var sut: GetCurrentBreathingExerciseUseCase

    @BeforeEach
    fun setUp() {
        sut = GetCurrentBreathingExerciseUseCase()
    }

    @Test
    fun `1- When invoked, then return ButeykoBreathing`() {
        // Act
        val result = sut()
        // Assert
        assertThat(result, `is`(instanceOf(ButeykoBreathing::class.java)))
    }
}
