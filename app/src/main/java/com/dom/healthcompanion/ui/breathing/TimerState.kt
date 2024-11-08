package com.dom.healthcompanion.ui.breathing

import com.dom.healthcompanion.domain.breathing.model.BreathingExercise

data class TimerState(
    val type: BreathingExercise.RoundType,
    val totalTimeText: String,
    val currentTimeText: String,
    val progress: Float,
    val laps: List<TimerLap> = emptyList(),
    val shouldKeepScreenOn: Boolean = false,
)
