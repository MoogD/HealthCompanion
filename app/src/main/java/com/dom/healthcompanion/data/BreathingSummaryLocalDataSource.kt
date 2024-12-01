package com.dom.healthcompanion.data

import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity
import com.dom.healthcompanion.data.database.breathing.BreathingSummaryDao

class BreathingSummaryLocalDataSource(
    private val breathingSummaryDao: BreathingSummaryDao,
) : BreathingSummaryDataSource {
    override suspend fun saveBreathingData(breathingData: BreathingDataEntity) {
        return breathingSummaryDao.insert(breathingData)
    }
}