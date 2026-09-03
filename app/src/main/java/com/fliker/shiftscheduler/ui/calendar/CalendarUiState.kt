package com.fliker.shiftscheduler.ui.calendar

import androidx.compose.runtime.Stable
import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import java.time.YearMonth

@Stable
data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val monthData: Map<YearMonth, List<WorkDay>> = emptyMap(),
    val patterns: List<ShiftPattern> = emptyList(),
    val selectedPattern: ShiftPattern? = null,
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
