package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.repository.ShiftRepository

class DeleteShiftPatternUseCase(
    private val repository: ShiftRepository
) {
    suspend operator fun invoke(patternId: Long) {
        repository.deletePattern(patternId)
    }
}
