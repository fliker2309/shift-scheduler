package com.fliker.shiftscheduler.ui.calendar

import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import java.time.YearMonth

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val days: List<WorkDay> = emptyList(),
    val availableTypes: List<ShiftType> = emptyList(),
    val isLoading: Boolean = false,
    val stats: MonthStats = MonthStats()
)

data class MonthStats(
    val totalWorkDays: Int = 0,
    val totalWorkHours: Double = 0.0,
    val dayShifts: Int = 0,
    val nightShifts: Int = 0
)
