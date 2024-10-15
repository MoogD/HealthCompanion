package com.dom.healthcompanion.ui.breathing

data class TimerState(
    val type: BreathingExercise.RoundType,
    val totalTimeText: String,
    val currentTimeText: String,
    val progress: Float,
    val laps: List<TimerLap> = emptyList(),
)
