package com.dom.healthcompanion.domain.breathing.usecase

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
    ) {
        suspend operator fun invoke(breathingData: BreathingSummary) {
            withContext(dispatchersProvider.io) {
                val data = BreathingDataEntity.fromBreathingSummary(breathingData)
                breathingSummaryDataSource.saveBreathingData(data)
            }
        }
    }
