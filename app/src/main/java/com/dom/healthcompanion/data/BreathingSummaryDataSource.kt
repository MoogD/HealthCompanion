package com.dom.healthcompanion.data

import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity

interface BreathingSummaryDataSource {
    suspend fun saveBreathingData(breathingData: BreathingDataEntity)
}
