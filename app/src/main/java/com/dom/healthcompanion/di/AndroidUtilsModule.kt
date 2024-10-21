package com.dom.healthcompanion.di

import android.content.Context
import com.dom.androidUtils.VibrationHelper
import com.dom.androidUtils.VibrationHelperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object AndroidUtilsModule {
    @Provides
    fun provideVibrationHelper(
        @ApplicationContext context: Context,
    ): VibrationHelper {
        return VibrationHelperImpl(context)
    }
}
