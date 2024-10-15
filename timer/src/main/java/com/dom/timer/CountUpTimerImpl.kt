package com.dom.timer

import com.dom.timer.CountUpTimer.Companion.DEFAULT_PERIOD
import com.dom.timer.CountUpTimer.Companion.NO_END_TIME
import com.dom.utils.DispatchersProvider
import com.dom.utils.DispatchersProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Timer class to CountUp from [startTimeInMillis] with 0 as default value. using kotlin coroutines.
 * Will call the [onTick] function of the provided listener (if [setListener] was called) every time the [periodInMillis] is reached (default to 1 second).
 * [periodInMillis] needs to be > 0.
 * [startTimeInMillis] can be provided to make the timer start at a time > 0 (default to 0).
 * [endTimeInMillis] can be provided to make the timer stop and clean up itself after the provided time.
 * [endTimeInMillis] needs to be bigger the 0 or it will be ignored.
 * If [endTimeInMillis] is provided and reached the [onFinish] function of the provided listener (if [setListener] was called) will be called.
 * */
class CountUpTimerImpl(
    private val startTimeInMillis: Long = 0L,
    private val periodInMillis: Long = DEFAULT_PERIOD,
    private val endTimeInMillis: Long = NO_END_TIME,
    dispatchersProvider: DispatchersProvider = DispatchersProviderImpl,
) : CountUpTimer {
    private var uiScope = CoroutineScope(dispatchersProvider.main + SupervisorJob())
    private var listener: CountUpTimer.Listener? = null

    /**
     * The current time of the timer. Precision is [periodInMillis] (will always be a multiple of [periodInMillis]).
     * */
    override var time = getStartTime()
        private set

    /**
     * Starts the timer by launching a new coroutine job that triggers calling the [onTick] function after a delay of [periodInMillis] in a loop.
     * Cancels previously started timer counts and restarts with [startTimeInMillis].
     * */
    override fun start() {
        stop()
        uiScope.launch {
            time = getStartTime()
            while (endTimeInMillis < 0 || time < endTimeInMillis) {
                delay(getPeriodOrDefault())
                time += periodInMillis
                listener?.onTick(time)
            }
            listener?.onFinish()
            stop()
        }
    }

    private fun getStartTime() = if (startTimeInMillis > 0) startTimeInMillis else 0L

    // Used to prevent endless loop for period = 0 or issues with negative delays
    private fun getPeriodOrDefault(): Long {
        return if (periodInMillis > 0) periodInMillis else DEFAULT_PERIOD
    }

    /**
     * Stops the timer by canceling the coroutine job and all potentially pending jobs.
     * */
    override fun stop() {
        uiScope.coroutineContext.cancelChildren()
    }

    override fun setListener(listener: CountUpTimer.Listener) {
        this.listener = listener
    }

    override fun removeListener(listener: CountUpTimer.Listener) {
        this.listener = null
    }
}
