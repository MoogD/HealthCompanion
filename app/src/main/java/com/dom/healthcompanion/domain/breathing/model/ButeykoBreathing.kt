package com.dom.healthcompanion.domain.breathing.model

import com.dom.healthcompanion.R
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise.Companion.OPEN_TIMER
import com.dom.healthcompanion.utils.TextString
import java.util.concurrent.TimeUnit

class ButeykoBreathing : BreathingExercise {
    override val title: TextString.Res = TextString.Res(R.string.buteyko_breathing_title)
    override val rounds: List<BreathingExercise.BreathingRound> =
        listOf(
            // First Round
            BreathingExercise.BreathingRound(OPEN_TIMER, BreathingExercise.RoundType.HOLD, false),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(2), BreathingExercise.RoundType.LOWER_BREATHING, true),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(1), BreathingExercise.RoundType.NORMAL_BREATHING, true),
            // Second Round
            BreathingExercise.BreathingRound(OPEN_TIMER, BreathingExercise.RoundType.HOLD, false),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(2), BreathingExercise.RoundType.LOWER_BREATHING, true),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(1), BreathingExercise.RoundType.NORMAL_BREATHING, true),
            // Third Round
            BreathingExercise.BreathingRound(OPEN_TIMER, BreathingExercise.RoundType.HOLD, false),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(2), BreathingExercise.RoundType.LOWER_BREATHING, true),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(1), BreathingExercise.RoundType.NORMAL_BREATHING, true),
            // Fourth Round
            BreathingExercise.BreathingRound(OPEN_TIMER, BreathingExercise.RoundType.HOLD, false),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(2), BreathingExercise.RoundType.LOWER_BREATHING, true),
            BreathingExercise.BreathingRound(TimeUnit.MINUTES.toMillis(1), BreathingExercise.RoundType.NORMAL_BREATHING, true),
            // Fifth Round
            BreathingExercise.BreathingRound(OPEN_TIMER, BreathingExercise.RoundType.HOLD, false),
            BreathingExercise.BreathingRound(TimeUnit.SECONDS.toMillis(30), BreathingExercise.RoundType.LOWER_BREATHING, true),
        )
    override var currenRoundIndex: Int = 0
}
