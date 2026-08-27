package com.fliker.shiftscheduler.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.usecase.GetScheduleForMonthUseCase
import com.fliker.shiftscheduler.domain.usecase.SetOverrideDayUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val getScheduleUseCase: GetScheduleForMonthUseCase,
    private val setOverrideDayUseCase: SetOverrideDayUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var scheduleJob: Job? = null

    init {
        loadMonthData(_uiState.value.yearMonth)
    }

    fun nextMonth() {
        val next = _uiState.value.yearMonth.plusMonths(1)
        loadMonthData(next)
    }

    fun previousMonth() {
        val prev = _uiState.value.yearMonth.minusMonths(1)
        loadMonthData(prev)
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        scheduleJob?.cancel()
        _uiState.update { it.copy(yearMonth = yearMonth, isLoading = true) }

        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        scheduleJob = getScheduleUseCase(start, end)
            .onEach { days ->
                val availableTypes = days.map { it.shiftType }.distinctBy { it.id } + 
                                     listOf(ShiftType.Off, ShiftType.Vacation, ShiftType.SickLeave)
                _uiState.update { 
                    it.copy(
                        days = days,
                        availableTypes = availableTypes.distinctBy { t -> t.id },
                        isLoading = false,
                        stats = calculateStats(days)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun applyOverride(date: LocalDate, shiftType: ShiftType) {
        viewModelScope.launch {
            setOverrideDayUseCase.save(
                WorkDay(
                    date = date,
                    shiftType = shiftType,
                    isCustomOverride = true
                )
            )
        }
    }

    fun clearOverride(date: LocalDate) {
        viewModelScope.launch {
            setOverrideDayUseCase.delete(date)
        }
    }

    private fun calculateStats(days: List<WorkDay>): MonthStats {
        var workDays = 0
        var workHours = 0.0
        var dayShifts = 0
        var nightShifts = 0

        days.forEach { day ->
            val type = day.shiftType
            if (type is ShiftType.Work) {
                workDays++
                val duration = Duration.between(type.startTime, type.endTime)
                val hours = if (duration.isNegative) {
                    duration.plusDays(1).toMinutes() / 60.0
                } else {
                    duration.toMinutes() / 60.0
                }
                workHours += hours
                
                // Simple logic: if start time is after 18:00 or before 06:00, it's night
                if (type.startTime.hour >= 18 || type.startTime.hour < 6) {
                    nightShifts++
                } else {
                    dayShifts++
                }
            }
        }

        return MonthStats(workDays, workHours, dayShifts, nightShifts)
    }
}
