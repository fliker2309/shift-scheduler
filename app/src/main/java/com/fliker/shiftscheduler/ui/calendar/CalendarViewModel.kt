package com.fliker.shiftscheduler.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.usecase.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap

class CalendarViewModel(
    val getScheduleUseCase: GetScheduleForMonthUseCase,
    private val setOverrideDayUseCase: SetOverrideDayUseCase,
    private val getShiftPatternsUseCase: GetShiftPatternsUseCase,
    private val selectActivePatternUseCase: SelectActivePatternUseCase,
    private val deleteShiftPatternUseCase: DeleteShiftPatternUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val jobs = ConcurrentHashMap<YearMonth, Job>()

    init {
        observePatterns()
        loadMonthData(_uiState.value.yearMonth)
    }

    private fun observePatterns() {
        combine(
            getShiftPatternsUseCase(),
            getScheduleUseCase.repository.getActivePattern()
        ) { allPatterns, activePattern ->
            if (activePattern?.id != _uiState.value.selectedPattern?.id) {
                clearCacheAndReload()
            }
            
            _uiState.update { 
                it.copy(
                    patterns = allPatterns,
                    selectedPattern = activePattern
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun clearCacheAndReload() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        _uiState.update { it.copy(monthData = emptyMap()) }
        loadMonthData(_uiState.value.yearMonth)
    }

    fun nextMonth() {
        updateCurrentMonth(_uiState.value.yearMonth.plusMonths(1))
    }

    fun previousMonth() {
        updateCurrentMonth(_uiState.value.yearMonth.minusMonths(1))
    }

    fun updateCurrentMonth(yearMonth: YearMonth) {
        if (_uiState.value.yearMonth == yearMonth) return
        
        _uiState.update { currentState ->
            val dataForMonth = currentState.monthData[yearMonth]
            currentState.copy(
                yearMonth = yearMonth,
                // CRITICAL FIX: Update stats immediately if data is already cached
                stats = if (dataForMonth != null) calculateStats(dataForMonth, yearMonth) else currentState.stats
            )
        }
        
        loadMonthData(yearMonth)
        loadMonthData(yearMonth.plusMonths(1))
        loadMonthData(yearMonth.minusMonths(1))
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        if (jobs.containsKey(yearMonth)) return

        val firstDayOfMonth = yearMonth.atDay(1)
        val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
        val start = firstDayOfMonth.minusDays(dayOfWeekOffset.toLong())
        val end = start.plusDays(41)

        val job = getScheduleUseCase(start, end)
            .onEach { days ->
                _uiState.update { currentState ->
                    val newMonthData = currentState.monthData + (yearMonth to days)
                    
                    // Update stats if this is the month we are currently looking at
                    val stats = if (yearMonth == currentState.yearMonth) {
                        calculateStats(days, yearMonth)
                    } else {
                        currentState.stats
                    }
                    
                    val activeMonthDays = newMonthData[currentState.yearMonth] ?: emptyList()
                    val availableTypes = activeMonthDays.map { it.shiftType }.distinctBy { it.id } + 
                                         listOf(ShiftType.Off, ShiftType.Vacation, ShiftType.SickLeave)

                    currentState.copy(
                        monthData = newMonthData,
                        availableTypes = availableTypes.distinctBy { t -> t.id },
                        stats = stats
                    )
                }
            }
            .launchIn(viewModelScope)
        
        jobs[yearMonth] = job
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
