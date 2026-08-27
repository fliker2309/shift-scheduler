package com.fliker.shiftscheduler.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.usecase.GetScheduleForMonthUseCase
import com.fliker.shiftscheduler.domain.usecase.GetShiftPatternsUseCase
import com.fliker.shiftscheduler.domain.usecase.SelectActivePatternUseCase
import com.fliker.shiftscheduler.domain.usecase.DeleteShiftPatternUseCase
import com.fliker.shiftscheduler.domain.usecase.SetOverrideDayUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val getScheduleUseCase: GetScheduleForMonthUseCase,
    private val setOverrideDayUseCase: SetOverrideDayUseCase,
    private val getShiftPatternsUseCase: GetShiftPatternsUseCase,
    private val selectActivePatternUseCase: SelectActivePatternUseCase,
    private val deleteShiftPatternUseCase: DeleteShiftPatternUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var scheduleJob: Job? = null

    init {
        observePatterns()
        loadMonthData(_uiState.value.yearMonth)
    }

    private fun observePatterns() {
        combine(
            getShiftPatternsUseCase(),
            getScheduleUseCase.repository.getActivePattern()
        ) { allPatterns, activePattern ->
            _uiState.update { 
                it.copy(
                    patterns = allPatterns,
                    selectedPattern = activePattern
                )
            }
        }.launchIn(viewModelScope)
    }

    fun selectPattern(patternId: Long) {
        viewModelScope.launch {
            selectActivePatternUseCase(patternId)
        }
    }

    fun deletePattern(patternId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val isActive = currentState.selectedPattern?.id == patternId
            
            deleteShiftPatternUseCase(patternId)
            
            if (isActive) {
                val remaining = currentState.patterns.filter { it.id != patternId }
                if (remaining.isNotEmpty()) {
                    selectActivePatternUseCase(remaining.first().id)
                }
            }
        }
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

        val firstDayOfMonth = yearMonth.atDay(1)
        val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
        val start = firstDayOfMonth.minusDays(dayOfWeekOffset.toLong())
        val end = start.plusDays(41) // Fetch 6 full weeks to cover any month grid

        scheduleJob = getScheduleUseCase(start, end)
            .onEach { days ->
                val availableTypes = days.map { it.shiftType }.distinctBy { it.id } + 
                                     listOf(ShiftType.Off, ShiftType.Vacation, ShiftType.SickLeave)
                _uiState.update { 
                    it.copy(
                        days = days,
                        availableTypes = availableTypes.distinctBy { t -> t.id },
                        isLoading = false,
                        stats = calculateStats(days, yearMonth)
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

    private fun calculateStats(days: List<WorkDay>, yearMonth: YearMonth): MonthStats {
        var workDays = 0
        var workHours = 0.0
        var dayShifts = 0
        var nightShifts = 0

        days.filter { YearMonth.from(it.date) == yearMonth }.forEach { day ->
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
