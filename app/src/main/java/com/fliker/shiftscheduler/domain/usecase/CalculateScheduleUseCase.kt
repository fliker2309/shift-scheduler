package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.WorkDay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CalculateScheduleUseCase {

    operator fun invoke(
        startDate: LocalDate,
        pattern: ShiftPattern,
        fromDay: LocalDate,
        toDay: LocalDate
    ): List<WorkDay> {
        if (pattern.items.isEmpty()) return emptyList()

        val result = mutableListOf<WorkDay>()
        var current = fromDay

        while (!current.isAfter(toDay)) {
            val daysBetween = ChronoUnit.DAYS.between(startDate, current)
            val patternIndex = Math.floorMod(daysBetween, pattern.durationDays.toLong()).toInt()
            val shiftType = pattern.items[patternIndex]

            result.add(
                WorkDay(
                    date = current,
                    shiftType = shiftType
                )
            )
            current = current.plusDays(1)
        }

        return result
    }
}