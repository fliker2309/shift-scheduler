package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.repository.ShiftRepository

class SelectActivePatternUseCase(
    private val repository: ShiftRepository
) {
    suspend operator fun invoke(patternId: Long) {
        repository.setActivePattern(patternId)
    }
}
