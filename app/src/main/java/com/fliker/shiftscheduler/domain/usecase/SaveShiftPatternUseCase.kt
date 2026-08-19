package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.repository.ShiftRepository

class SaveShiftPatternUseCase(
    private val repository: ShiftRepository
) {
    suspend operator fun invoke(pattern: ShiftPattern) {
        repository.savePattern(pattern)
    }
}
