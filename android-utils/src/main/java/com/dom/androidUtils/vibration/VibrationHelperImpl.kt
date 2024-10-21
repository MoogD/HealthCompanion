package com.dom.androidUtils.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

/**
 * Requires [android.permission.VIBRATE] permission.
 * Uses [Vibrator] to vibrate the device.
* */
class VibrationHelperImpl(context: Context) : VibrationHelper {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    override fun vibrate(type: VibrationHelper.VibrationType) {
        when (type) {
            VibrationHelper.VibrationType.NOTIFY_USER ->
                vibrator.vibrate(VibrationEffect.createOneShot(DEFAULT_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    companion object {
        internal const val DEFAULT_DURATION = 100L
    }
}
