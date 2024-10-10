package com.dom.timer

interface CountUpTimer {
    val time: Long

    fun start()

    fun stop()

    fun setListener(listener: Listener)

    fun removeListener(listener: Listener)

    interface Listener {
        fun onTick(time: Long)

        fun onFinish() {}
    }

    companion object {
        const val NO_END_TIME = -1L

        // default period
        const val DEFAULT_PERIOD = 1000L
    }
}
