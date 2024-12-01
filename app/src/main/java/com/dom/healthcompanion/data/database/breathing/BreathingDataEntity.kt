package com.dom.healthcompanion.data.database.breathing

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary

@Entity(tableName = DbConstants.BREATHING_TABLE_NAME)
@TypeConverters(BreathingConverter::class)
data class BreathingDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "rounds") val rounds: List<BreathingSummary.BreathingRoundSummary>,
) {
    companion object {
        fun fromBreathingSummary(breathingSummary: BreathingSummary): BreathingDataEntity {
            return BreathingDataEntity(
                title = breathingSummary.title,
                rounds = breathingSummary.rounds,
            )
        }
    }
}
