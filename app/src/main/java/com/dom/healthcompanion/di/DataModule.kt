package com.dom.healthcompanion.di

import android.content.Context
import androidx.room.Room
import com.dom.healthcompanion.data.BreathingSummaryDataSource
import com.dom.healthcompanion.data.BreathingSummaryLocalDataSource
import com.dom.healthcompanion.data.database.AppDatabase
import com.dom.healthcompanion.data.database.breathing.BreathingSummaryDao
import com.dom.healthcompanion.data.database.breathing.BreathingConverter
import com.dom.healthcompanion.data.database.breathing.DbConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideBreathingConverter(): BreathingConverter {
        return BreathingConverter()
    }

    @Provides
    fun provideAppDb(
        @ApplicationContext context: Context,
        converters: BreathingConverter,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DbConstants.DB_NAME,
        )
            .addTypeConverter(converters)
            .build()
    }

    @Provides
    fun provideBreathingDao(db: AppDatabase): BreathingSummaryDao {
        return db.breathingDao()
    }

    @Provides
    fun provideBreathingDataSource(breathingSummaryDao: BreathingSummaryDao): BreathingSummaryDataSource {
        return BreathingSummaryLocalDataSource(breathingSummaryDao)
    }
}
