package com.dom.healthcompanion.ui.breathing

import androidx.lifecycle.ViewModel
import com.dom.androidUtils.sound.SoundPlayer
import com.dom.androidUtils.vibration.VibrationHelper
import com.dom.healthcompanion.R
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise
import com.dom.healthcompanion.domain.breathing.usecase.GetCurrentBreathingExerciseUseCase
import com.dom.healthcompanion.domain.breathing.model.BreathingExercise.Companion.OPEN_TIMER
import com.dom.healthcompanion.utils.ButtonState
import com.dom.healthcompanion.utils.TextString
import com.dom.logger.Logger
import com.dom.timer.CountUpTimer
import com.dom.timer.CountUpTimerImpl
import com.dom.timer.millisToMinutesAndSeconds
import com.dom.utils.DispatchersProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BreathingViewModel
    @Inject
    constructor(
        private val getCurrentBreathingExerciseUseCase: GetCurrentBreathingExerciseUseCase,
        private val dispatchersProvider: DispatchersProvider,
        private val vibrationHelper: VibrationHelper,
        private val soundPlayer: SoundPlayer,
        private val logger: Logger,
    ) : ViewModel() {
        // region variables
        private var currentExercise: BreathingExercise = getCurrentBreathingExerciseUseCase()
        private var timer: CountUpTimerImpl? = null
        private val previousRounds = mutableListOf<Long>()
        private val currentRound
            get() = currentExercise.currentRound

        private val timerListener =
            object : CountUpTimer.Listener {
                override fun onTick(time: Long) {
                    this@BreathingViewModel.onTick(time)
                }

                override fun onFinish(trackedTime: Long) {
                    this@BreathingViewModel.onFinish(trackedTime)
                    notifyUserForNextRound()
                    logger.d("Timer done with $trackedTime tracked time")
                }
            }
        // endregion

        // region flows
        private val _timerStateFlow = MutableStateFlow(getInitialTimerState())
        val timerStateFlow: StateFlow<TimerState>
            get() = _timerStateFlow

        private val _buttonStateFlow = MutableStateFlow(getButtonState(true, false))
        val buttonStateFlow: StateFlow<ButtonState>
            get() = _buttonStateFlow

        private val _titleFlow = MutableStateFlow(currentExercise.title)
        val titleFlow: StateFlow<TextString>
            get() = _titleFlow
        // endregion

        init {
            soundPlayer.init(listOf(R.raw.hero_simple_celebration_03))
        }

        private fun getInitialTimerState() = TimerState(BreathingExercise.RoundType.IDLE, STARTING_TIME_STRING, STARTING_TIME_STRING, 0f, shouldKeepScreenOn = false)

        // region button functions
        private fun onStartClicked() {
            if (currentExercise.currenRoundIndex > 0) {
                currentExercise = getCurrentBreathingExerciseUseCase()
                previousRounds.clear()
                _timerStateFlow.value = getInitialTimerState()
            }
            _timerStateFlow.value = _timerStateFlow.value.copy(type = currentRound.type, shouldKeepScreenOn = true)
            cleanUpTimer()
            timer = createTimer()
            timer?.setListener(timerListener)
            timer?.start()
            updateButtonState(true)
            logger.d("Timer started")
        }

        private fun onNextClicked() {
            // clear potentially running timers:
            val trackedTime = timer?.stop() ?: 0L
            logger.d("next button clicked with $trackedTime trackedTime")
            onFinish(trackedTime)
        }

        private fun onPauseClicked() {
            timer?.pause()
            logger.d("next button clicked with ${timer?.time} trackedTime")
            _buttonStateFlow.value = ButtonState(TextString.Res(R.string.btnResumeText), ::onResumeClicked)
            _timerStateFlow.value = _timerStateFlow.value.copy(shouldKeepScreenOn = false)
            logger.d("buttonstate changed to resume state")
        }

        private fun onResumeClicked() {
            _buttonStateFlow.value = ButtonState(TextString.Res(R.string.btnPauseText), ::onPauseClicked)
            _timerStateFlow.value = _timerStateFlow.value.copy(shouldKeepScreenOn = true)
            logger.d("next button clicked with ${timer?.time} trackedTime. Show onPause button state.")
            timer?.resume()
        }
        // endregion

        // region timer functions
        private fun onTick(time: Long) {
            // show next button if not open timer but next round needs to be started by user
            val isNextButtonShown = (_buttonStateFlow.value.text as TextString.Res).resId == R.string.btnNextText
            if (!isNextButtonShown && currentRound.expectedTime != OPEN_TIMER && time >= currentRound.expectedTime) {
                logger.d("expected time exceeded and next round does not start automatically.")
                updateButtonState(trackedTime = time)
                notifyUserForNextRound()
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

        private fun notifyUserForNextRound() {
            vibrationHelper.vibrate(VibrationHelper.VibrationType.NOTIFY_USER)
            soundPlayer.play(R.raw.hero_simple_celebration_03)
            logger.d("notify user for next round")
        }

        private fun onFinish(trackedTime: Long) {
            logger.d("timer done with $trackedTime")
            updateRoundsData(trackedTime)
            timer?.stop()
            val hasNextRoundBeforeChanging = currentExercise.currenRoundIndex < currentExercise.rounds.lastIndex
            if (hasNextRoundBeforeChanging) {
                currentExercise.currenRoundIndex++
                logger.d("start new round with index ${currentExercise.currenRoundIndex} and type = ${currentRound.type}")
                _timerStateFlow.value = _timerStateFlow.value.copy(type = currentRound.type)
                cleanUpTimer()
                timer = createTimer()
                timer?.setListener(timerListener)
                timer?.start()
            } else {
                logger.d("exercise done. Show finished state!")
                _timerStateFlow.value =
                    _timerStateFlow.value.copy(
                        type = BreathingExercise.RoundType.FINISHED,
                        currentTimeText = STARTING_TIME_STRING,
                        shouldKeepScreenOn = false,
                    )
            }
            updateButtonState(hasNextRoundBeforeChanging, !hasNextRoundBeforeChanging, trackedTime)
        }
        // endregion

        private fun updateButtonState(
            isNewRound: Boolean = false,
            isDone: Boolean = false,
            trackedTime: Long? = null,
        ) {
            _buttonStateFlow.value =
                getButtonState(isNewRound, isDone, trackedTime)
            logger.d("buttonState updated to ${_buttonStateFlow.value}")
        }

        private fun getButtonState(
            isNewRound: Boolean,
            isDone: Boolean,
            trackedTime: Long? = null,
        ): ButtonState {
            val isOpenTimer = currentRound.expectedTime == OPEN_TIMER && !isDone
            val isExpectedTimeDone = isOpenTimer || (!isNewRound && (trackedTime ?: Long.MAX_VALUE) >= currentRound.expectedTime)
            logger.d("timer: $timer, currentRound = ${currentExercise.currentRound}, hasNextRound = ${currentExercise.hasNextRound}, doesNextRoundStartAutomatically: ${currentExercise.doesNextRoundStartAutomatically} isOpenTimer: $isOpenTimer, isExpectedTimeDone: $isExpectedTimeDone, isNewRound: $isNewRound, isDone: $isDone")
            return when {
                // Timer was not started
                timer == null && currentExercise.currenRoundIndex == 0 ->
                    ButtonState(TextString.Res(R.string.btnStartText), ::onStartClicked)

                // TODO: handle timer was started but canceled

                // Timer is active but user needs to start next round but current round is not done yet
                !isExpectedTimeDone && currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically ->
                    ButtonState(TextString.Res(R.string.btnPauseText), ::onPauseClicked)

                // Timer is active but user needs to start next round
                isExpectedTimeDone && currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically ->
                    ButtonState(TextString.Res(R.string.btnNextText), ::onNextClicked)

                // Timer is active and current round does not end automatically
                isOpenTimer ->
                    ButtonState(TextString.Res(R.string.btnNextText), ::onNextClicked)

                // Timer is active and next round starts automatically or there is no next round
                currentExercise.doesNextRoundStartAutomatically ->
                    ButtonState(TextString.Res(R.string.btnPauseText), ::onPauseClicked)

                // Timer is active and there is no next round and current round just started and ends automatically
                !currentExercise.hasNextRound && isNewRound ->
                    ButtonState(TextString.Res(R.string.btnPauseText), ::onPauseClicked)

                // Exercise is done
                else -> {
                    ButtonState(TextString.Res(R.string.btnStartText), ::onStartClicked)
                }
            }
        }

        private fun createTimer(): CountUpTimerImpl {
            val hasOpenTimer = currentRound.expectedTime == OPEN_TIMER
            val shouldUseEndTime = !hasOpenTimer && (!currentExercise.hasNextRound || currentExercise.doesNextRoundStartAutomatically)
            val endTimeInMillis =
                if (shouldUseEndTime) {
                    currentRound.expectedTime
                } else {
                    CountUpTimer.NO_END_TIME
                }
            logger.d("hasOpenTimer=  $hasOpenTimer, hasNextRound = ${currentExercise.hasNextRound}, doesNextRoundStartAutomatically = ${currentExercise.doesNextRoundStartAutomatically}, endTime = $endTimeInMillis")
            return CountUpTimerImpl(
                endTimeInMillis = endTimeInMillis,
                periodInMillis = TimeUnit.SECONDS.toMillis(1),
                dispatchersProvider = dispatchersProvider,
            )
        }

        private fun getTotalTimeFromPreviousRounds(): Long {
            return previousRounds.filterIndexed { index, _ -> index < currentExercise.currenRoundIndex }.sum()
        }

        private fun updateRoundsData(trackedTime: Long) {
            val newRound =
                if (currentRound.expectedTime == OPEN_TIMER || (currentExercise.hasNextRound && !currentExercise.doesNextRoundStartAutomatically)) {
                    trackedTime
                } else {
                    currentRound.expectedTime
                }
            previousRounds.add(newRound)
            val laps = mutableListOf<TimerLap>()
            previousRounds.forEachIndexed { index, roundTime ->
                laps.add(TimerLap(index + 1, roundTime.millisToMinutesAndSeconds()))
            }
            logger.d("new laps ${laps.joinToString { it.index.toString() + ": " + it.time }}")
            _timerStateFlow.value = _timerStateFlow.value.copy(laps = laps)
        }

        fun onStopClicked() {
            logger.d("onStopClicked reset timer and states.")
            cleanUpTimer()
            currentExercise = getCurrentBreathingExerciseUseCase()
            previousRounds.clear()
            _timerStateFlow.value = getInitialTimerState()
            updateButtonState(true)
        }

        override fun onCleared() {
            super.onCleared()
            cleanUpTimer()
            soundPlayer.destroy()
        }

        private fun cleanUpTimer() {
            timer?.stop()
            timer?.removeListener(timerListener)
            timer = null
        }

        companion object {
            const val STARTING_TIME_STRING = "00:00"
        }
    }
