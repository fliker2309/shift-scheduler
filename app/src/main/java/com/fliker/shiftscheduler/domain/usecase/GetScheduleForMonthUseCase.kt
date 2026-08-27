package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class GetScheduleForMonthUseCase(
    val repository: ShiftRepository
) {
    operator fun invoke(from: LocalDate, to: LocalDate): Flow<List<WorkDay>> {
        return combine(
            repository.getActivePattern(),
            repository.getCustomOverrides(from, to)
        ) { pattern, overrides ->
            if (pattern == null || pattern.items.isEmpty()) return@combine emptyList<WorkDay>()

            val result = mutableListOf<WorkDay>()
            var current = from
            val overridesMap = overrides.associateBy { it.date }

            while (!current.isAfter(to)) {
                val override = overridesMap[current]
                if (override != null) {
                    result.add(override)
                } else {
                    val daysBetween = current.toEpochDay() - pattern.startDateEpochDay
                    val patternIndex = Math.floorMod(daysBetween, pattern.items.size.toLong()).toInt()
                    result.add(
                        WorkDay(
                            date = current,
                            shiftType = pattern.items[patternIndex]
                        )
                    )
                }
                current = current.plusDays(1)
            }
            result
        }
    }
}
