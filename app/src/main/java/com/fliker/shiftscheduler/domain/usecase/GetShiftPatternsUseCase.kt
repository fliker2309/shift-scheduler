package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow

class GetShiftPatternsUseCase(
    private val repository: ShiftRepository
) {
    operator fun invoke(): Flow<List<ShiftPattern>> {
        // We'll need to update repository to provide all patterns
        return repository.getAllPatterns()
    }
}
