package com.dom.healthcompanion.domain.breathing.model

data class BreathingSummary(
    val title: String,
    val rounds: List<BreathingRoundSummary>,
) {
    data class BreathingRoundSummary(
        val type: BreathingExercise.RoundType,
        val expectedTime: Long,
        val actualTime: Long,
    )
}
