package com.dom.healthcompanion.domain.breathing.model

import com.dom.healthcompanion.domain.breathing.model.BreathingSummary.RoundType.EXHALE
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary.RoundType.HOLD
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary.RoundType.INHALE
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary.RoundType.LOWER_BREATHING
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary.RoundType.NORMAL_BREATHING
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class BreathingSummaryTest {
    // region test cases

    // 1- When from invoked, given roundType exists for type, then return correct RoundType
    // 2- When from invoked, given roundType doesnt exist for type, then return null

    // endregion

    @Nested
    @DisplayName("When from invoked")
    inner class From {
        @ParameterizedTest
        @EnumSource(
            BreathingExercise.RoundType::class,
            names = ["IDLE", "PAUSE", "FINISHED"],
            mode = EnumSource.Mode.EXCLUDE,
        )
        fun `1-given roundType exists for type, then return correct RoundType`(type: BreathingExercise.RoundType) {
            // Arrange
            val expected =
                when (type) {
                    BreathingExercise.RoundType.INHALE -> INHALE
                    BreathingExercise.RoundType.EXHALE -> EXHALE
                    BreathingExercise.RoundType.HOLD -> HOLD
                    BreathingExercise.RoundType.LOWER_BREATHING -> LOWER_BREATHING
                    BreathingExercise.RoundType.NORMAL_BREATHING -> NORMAL_BREATHING
                    else -> null
                } ?: assert(false) { "Unexpected RoundType" }
            // Act
            val result = BreathingSummary.RoundType.from(type)
            // Assert
            assertThat(result, `is`(expected))
        }

        @ParameterizedTest
        @EnumSource(BreathingExercise.RoundType::class, names = ["IDLE", "PAUSE", "FINISHED"], mode = EnumSource.Mode.INCLUDE)
        fun `2- given roundType doesnt exist for type, then return null`(type: BreathingExercise.RoundType) {
            // Act
            val result = BreathingSummary.RoundType.from(type)
            // Assert
            assertThat(result, `is`(nullValue()))
        }
    }
}
