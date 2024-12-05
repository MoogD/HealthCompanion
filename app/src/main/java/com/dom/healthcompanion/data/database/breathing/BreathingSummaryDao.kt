package com.dom.healthcompanion.data.database.breathing

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BreathingSummaryDao {
    @Query("SELECT * FROM ${DbConstants.BREATHING_TABLE_NAME}")
    fun getAll(): List<BreathingDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(breathingDataEntity: BreathingDataEntity)
}
