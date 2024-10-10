package com.dom.healthcompanion.domain.breathing.usecase

import com.dom.healthcompanion.domain.breathing.model.BreathingExercise
import com.dom.healthcompanion.domain.breathing.model.ButeykoBreathing
import javax.inject.Inject

class GetCurrentBreathingExerciseUseCase
    @Inject
    constructor() {
        operator fun invoke(): BreathingExercise = ButeykoBreathing()
    }
