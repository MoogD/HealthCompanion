package com.dom.androidUtils

interface VibrationHelper {
    fun vibrate(type: VibrationType)

    enum class VibrationType {
        NOTIFY_USER,
    }
}
