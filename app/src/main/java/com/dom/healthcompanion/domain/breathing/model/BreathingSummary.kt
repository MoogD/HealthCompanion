package com.dom.healthcompanion.domain.breathing.model

data class BreathingSummary(
    val title: String,
    val rounds: List<BreathingRoundSummary>,
) {
    data class BreathingRoundSummary(
        val type: RoundType,
        val expectedTime: Long,
        val actualTime: Long,
    )

    enum class RoundType {
        INHALE,
        EXHALE,
        HOLD,
        LOWER_BREATHING,
        NORMAL_BREATHING, ;

        companion object {
            fun from(type: BreathingExercise.RoundType): RoundType? =
                when (type) {
                    BreathingExercise.RoundType.INHALE -> INHALE
                    BreathingExercise.RoundType.EXHALE -> EXHALE
                    BreathingExercise.RoundType.HOLD -> HOLD
                    BreathingExercise.RoundType.LOWER_BREATHING -> LOWER_BREATHING
                    BreathingExercise.RoundType.NORMAL_BREATHING -> NORMAL_BREATHING
                    BreathingExercise.RoundType.IDLE,
                    BreathingExercise.RoundType.PAUSE,
                    BreathingExercise.RoundType.FINISHED,
                    -> null
                }
        }
    }
}
