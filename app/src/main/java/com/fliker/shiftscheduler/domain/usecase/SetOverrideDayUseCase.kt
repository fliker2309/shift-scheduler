package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import java.time.LocalDate

class SetOverrideDayUseCase(
    private val repository: ShiftRepository
) {
    suspend fun save(workDay: WorkDay) {
        repository.saveCustomOverride(workDay)
    }

    suspend fun delete(date: LocalDate) {
        repository.deleteCustomOverride(date)
    }
}
