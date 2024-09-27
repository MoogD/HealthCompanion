package com.dom.healthcompanion.ui.breathing

import androidx.lifecycle.ViewModel
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.breathing.BreathingExercise.Companion.OPEN_TIMER
import com.dom.healthcompanion.utils.ButtonState
import com.dom.healthcompanion.utils.Text
import com.dom.timer.CountUpTimer
import com.dom.timer.millisToMinutesAndSeconds
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BreathingViewModel
    @Inject
    constructor() : ViewModel() {
        // region variables
        private var currentExercise: BreathingExercise = ButeykoBreathing()
        private var timer: CountUpTimer? = null
        private val previousRounds = mutableListOf<Long>()
        private val currentRound
            get() = currentExercise.currentRound
        // endregion

        // region flows
        private val _timerStateFlow = MutableStateFlow(getInitialTimerState())
        val timerStateFlow: StateFlow<TimerState>
            get() = _timerStateFlow

        private val _buttonStateFlow = MutableStateFlow(ButtonState(Text.TextRes(R.string.btnStartText), ::onStartClicked))
        val buttonStateFlow: StateFlow<ButtonState>
            get() = _buttonStateFlow

        private val _titleFlow = MutableStateFlow(currentExercise.title)
        val titleFlow: StateFlow<Text>
            get() = _titleFlow
        // endregion

        private fun getInitialTimerState() = TimerState(currentRound.type, STARTING_TIME_STRING, STARTING_TIME_STRING, 0f)

        // region button functions
        private fun onStartClicked() {
            // restart with first round if already started
            currentExercise.currenRoundIndex = 0
            previousRounds.clear()
            _timerStateFlow.value = getInitialTimerState()
            timer = createTimer()
            timer?.start()
            updateButtonState(true)
        }

        private fun onNextClicked() {
            // clear potentially running timers:
            timer?.stop()
            onFinish()
        }

        private fun onPauseClicked() {
        }
        // endregion

        // region timer functions
        private fun onTick(time: Long) {
            // show next button if not open timer but next round needs to be started by user
            if (currentRound.expectedTime != OPEN_TIMER && time > currentRound.expectedTime) {
                updateButtonState()
            }
            val progress =
                if (currentRound.expectedTime > 0) {
                    time.toFloat() / currentRound.expectedTime
                } else {
                    0f
                }
            val currentTime = time.millisToMinutesAndSeconds()
            val totalTime = (time + getTotalTimeFromPreviousRounds()).millisToMinutesAndSeconds()
            _timerStateFlow.value =
                _timerStateFlow.value.copy(
                    currentTimeText = currentTime,
                    totalTimeText = totalTime,
                    progress = progress,
                )
        }

        private fun onFinish() {
            updateRoundsData()
            timer?.stop()
            val hasNextRoundBeforeChanging = currentExercise.currenRoundIndex < currentExercise.rounds.lastIndex
            if (hasNextRoundBeforeChanging) {
                currentExercise.currenRoundIndex++
                _timerStateFlow.value = _timerStateFlow.value.copy(type = currentRound.type)
                timer = createTimer()
                timer?.start()
            } else {
                // TODO: show finished state
                _timerStateFlow.value =
                    _timerStateFlow.value.copy(
                        type = BreathingExercise.RoundType.FINISHED,
                        currentTimeText = STARTING_TIME_STRING,
                    )
            }
            updateButtonState(hasNextRoundBeforeChanging)
        }
        // endregion

        private fun updateButtonState(isNewRound: Boolean = false) {
            val isExpectedTimeDone = !isNewRound && (timer?.time ?: Long.MAX_VALUE) > currentRound.expectedTime
            _buttonStateFlow.value =
                when {
                    // Timer was not started
                    timer == null && currentExercise.currenRoundIndex == 0 ->
                        ButtonState(Text.TextRes(R.string.btnStartText), ::onStartClicked)

                    // TODO: handle timer was started but canceled

                    // Timer is active but user needs to start next round but current round is not done yet
                    !isExpectedTimeDone && currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically ->
                        ButtonState(Text.TextRes(R.string.btnPauseText), ::onPauseClicked)

                    // Timer is active but user needs to start next round
                    isExpectedTimeDone && currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically ->
                        ButtonState(Text.TextRes(R.string.btnNextText), ::onNextClicked)
                    // Timer is active and current round does not end automatically
                    currentRound.expectedTime == OPEN_TIMER ->
                        ButtonState(Text.TextRes(R.string.btnNextText), ::onNextClicked)

                    // Timer is active and next round starts automatically or there is no next round
                    currentExercise.doesNextRoundStartAutomatically ->
                        ButtonState(Text.TextRes(R.string.btnPauseText), ::onPauseClicked)

                    // Timer is active and there is no next round and current round just started and ends automatically
                    !currentExercise.hasNextRound && isNewRound ->
                        ButtonState(Text.TextRes(R.string.btnPauseText), ::onPauseClicked)

                    // Exercise is done
                    else -> {
                        ButtonState(Text.TextRes(R.string.btnStartText), ::onStartClicked)
                    }
                }
        }

        private fun createTimer(): CountUpTimer {
            val hasOpenTimer = currentRound.expectedTime == OPEN_TIMER
            val shouldUseEndTime = !hasOpenTimer && (!currentExercise.hasNextRound || currentExercise.doesNextRoundStartAutomatically)
            val endTimeInMillis =
                if (shouldUseEndTime) {
                    currentRound.expectedTime
                } else {
                    CountUpTimer.NO_END_TIME
                }
            return CountUpTimer(
                onTick = ::onTick,
                endTimeInMillis = endTimeInMillis,
                onFinish = ::onFinish,
                periodInMillis = TimeUnit.SECONDS.toMillis(1),
            )
        }

        private fun getTotalTimeFromPreviousRounds(): Long {
            return previousRounds.filterIndexed { index, _ -> index < currentExercise.currenRoundIndex }.sum()
        }

        private fun updateRoundsData() {
            val newRound =
                if (currentRound.expectedTime == OPEN_TIMER || (currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically)) {
                    timer?.time ?: 0L
                } else {
                    currentRound.expectedTime
                }
            previousRounds.add(newRound)
            val laps = mutableListOf<TimerLap>()
            previousRounds.forEachIndexed { index, roundTime ->
                laps.add(TimerLap(index + 1, roundTime.millisToMinutesAndSeconds()))
            }
            _timerStateFlow.value = _timerStateFlow.value.copy(laps = laps)
        }

        override fun onCleared() {
            super.onCleared()
            timer?.stop()
            timer = null
        }

        companion object {
            const val STARTING_TIME_STRING = "00:00"
        }
    }
