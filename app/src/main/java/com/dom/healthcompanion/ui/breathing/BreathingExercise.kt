package com.dom.healthcompanion.ui.breathing

import com.dom.healthcompanion.utils.Text

interface BreathingExercise {
    val title: Text
    val rounds: List<BreathingRound>
    var currenRoundIndex: Int

    val currentRound: BreathingRound
        get() = rounds[currenRoundIndex]
    val hasNextRound: Boolean
        get() = currenRoundIndex < rounds.lastIndex
    val doesNextRoundStartAutomatically: Boolean
        get() = hasNextRound && rounds[currenRoundIndex + 1].startAutomatically

    data class BreathingRound(
        val expectedTime: Long,
        val type: RoundType,
        val startAutomatically: Boolean,
    )

    enum class RoundType {
        INHALE,
        EXHALE,
        HOLD,
        PAUSE,
        LOWER_BREATHING,
        NORMAL_BREATHING,
        FINISHED,
    }

    companion object {
        const val OPEN_TIMER: Long = -1
    }
}
