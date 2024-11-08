package com.dom.androidUtils.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.dom.logger.Logger

/**
 * Requires [android.permission.VIBRATE] permission.
 * Uses [Vibrator] to vibrate the device.
* */
class VibrationHelperImpl(context: Context, private val logger: Logger) : VibrationHelper {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    override fun vibrate(type: VibrationHelper.VibrationType) {
        logger.d("vibrate $type")
        when (type) {
            VibrationHelper.VibrationType.NOTIFY_USER ->
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(DEFAULT_DURATION, DEFAULT_DURATION), NO_REPETITION),
                )
        }
    }

    companion object {
        internal const val DEFAULT_DURATION = 300L

        // value from https://developer.android.com/reference/android/os/VibrationEffect#createWaveform(long[],%20int)
        internal const val NO_REPETITION = -1
    }
}
