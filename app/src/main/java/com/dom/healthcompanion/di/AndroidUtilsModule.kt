package com.dom.healthcompanion.di

import android.content.Context
import com.dom.androidUtils.logger.TimberLogger
import com.dom.androidUtils.sound.SoundPlayer
import com.dom.androidUtils.sound.SoundPlayerImpl
import com.dom.androidUtils.vibration.VibrationHelper
import com.dom.androidUtils.vibration.VibrationHelperImpl
import com.dom.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AndroidUtilsModule {
    @Provides
    fun provideLogger(): Logger {
        return TimberLogger
    }

    @Provides
    fun provideVibrationHelper(
        @ApplicationContext context: Context,
        logger: Logger,
    ): VibrationHelper {
        return VibrationHelperImpl(context, logger)
    }

    @Provides
    fun provideSoundPlayer(
        @ApplicationContext context: Context,
        logger: Logger,
    ): SoundPlayer {
        return SoundPlayerImpl(context, logger)
    }
}
