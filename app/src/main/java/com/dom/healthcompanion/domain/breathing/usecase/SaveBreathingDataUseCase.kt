package com.dom.healthcompanion.domain.breathing.usecase

import com.dom.androidUtils.time.TimeHelper
import com.dom.healthcompanion.data.BreathingSummaryDataSource
import com.dom.healthcompanion.data.database.breathing.BreathingDataEntity
import com.dom.healthcompanion.domain.breathing.model.BreathingSummary
import com.dom.utils.DispatchersProvider
import javax.inject.Inject
import kotlinx.coroutines.withContext

class SaveBreathingDataUseCase
    @Inject
    constructor(
        private val breathingSummaryDataSource: BreathingSummaryDataSource,
        private val dispatchersProvider: DispatchersProvider,
        private val timeHelper: TimeHelper,
    ) {
        suspend operator fun invoke(breathingData: BreathingSummary) {
            withContext(dispatchersProvider.io) {
                val timestamp = timeHelper.getCurrentTimeMillis()
                val data = BreathingDataEntity.fromBreathingSummary(breathingData, timestamp)
                breathingSummaryDataSource.saveBreathingData(data)
            }
        }
    }
