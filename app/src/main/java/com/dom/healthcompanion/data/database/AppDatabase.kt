package com.dom.healthcompanion.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dom.healthcompanion.data.database.breathing.BreathingSummaryDao
import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity
import com.dom.healthcompanion.data.database.breathing.BreathingConverter

@Database(entities = [BreathingDataEntity::class], version = 1)
@TypeConverters(BreathingConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breathingDao(): BreathingSummaryDao
}
