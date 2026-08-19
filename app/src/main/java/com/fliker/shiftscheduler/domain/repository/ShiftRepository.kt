package com.fliker.shiftscheduler.domain.repository

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.WorkDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ShiftRepository {
    fun getActivePattern(): Flow<ShiftPattern?>
    suspend fun savePattern(pattern: ShiftPattern)

    fun getCustomOverrides(from: LocalDate, to: LocalDate): Flow<List<WorkDay>>
    suspend fun saveCustomOverride(workDay: WorkDay)
    suspend fun deleteCustomOverride(date: LocalDate)
}