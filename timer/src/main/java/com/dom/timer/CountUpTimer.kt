package com.dom.timer

interface CountUpTimer {
    val time: Long

    fun start()

    fun pause()

    fun resume()

    fun stop(): Long

    fun setListener(listener: Listener)

    fun removeListener(listener: Listener)

    interface Listener {
        fun onTick(time: Long)

        fun onFinish(trackedTime: Long) {}
    }

    companion object {
        const val NO_END_TIME = -1L

        // default period
        const val DEFAULT_PERIOD = 1000L
    }
}
