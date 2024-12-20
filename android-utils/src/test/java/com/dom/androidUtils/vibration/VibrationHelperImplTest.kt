package com.dom.androidUtils.vibration

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.dom.androidUtils.vibration.VibrationHelperImpl.Companion.DEFAULT_DURATION
import com.dom.androidUtils.vibration.VibrationHelperImpl.Companion.NO_REPETITION
import com.dom.logger.Logger
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class VibrationHelperImplTest {
    // region test cases

    // 1- When vibrate invoked, then call vibrator vibrate with expected VibrationEffect

    // endregion

    private val mockkContext: Context = mockk()
    private val mockkVibrator: Vibrator = mockk()
    private val logger = mockk<Logger>(relaxed = true)
    private lateinit var sut: VibrationHelperImpl

    @BeforeEach
    fun setUp() {
        every { mockkContext.getSystemService(Vibrator::class.java) } returns mockkVibrator
        sut = VibrationHelperImpl(mockkContext, logger)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @ParameterizedTest
    @EnumSource(VibrationHelper.VibrationType::class)
    fun `1- When vibrate invoked, then call vibrator vibrate with expected VibrationEffect`(type: VibrationHelper.VibrationType) {
        // Arrange
        val expectedVibrationEffect = getVibrationEffectForType(type)
        justRun { mockkVibrator.vibrate(expectedVibrationEffect) }
        // Act
        sut.vibrate(type)
        // Assert
        every { mockkVibrator.vibrate(expectedVibrationEffect) }
    }

    // region helper function
    private fun getVibrationEffectForType(type: VibrationHelper.VibrationType): VibrationEffect {
        mockkStatic(VibrationEffect::class)
        val (lengtht, amplitude) =
            when (type) {
                VibrationHelper.VibrationType.NOTIFY_USER ->
                    Pair(DEFAULT_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
            }
        val expectedEffect = mockk<VibrationEffect>()
        every { VibrationEffect.createWaveform(longArrayOf(DEFAULT_DURATION, DEFAULT_DURATION), NO_REPETITION) } returns expectedEffect
        return expectedEffect
    }
    // endregion
}
