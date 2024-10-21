package com.dom.androidUtils.vibration

interface VibrationHelper {
    fun vibrate(type: VibrationType)

    enum class VibrationType {
        NOTIFY_USER,
    }
}
