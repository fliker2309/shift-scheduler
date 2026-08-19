package com.fliker.shiftscheduler.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.usecase.SaveShiftPatternUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class PatternSettingsViewModel(
    private val saveShiftPatternUseCase: SaveShiftPatternUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternSettingsUiState(
        availableTypes = listOf(
            ShiftType.Work(
                id = UUID.randomUUID().toString(),
                name = "Дневная",
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(20, 0),
                colorHex = "#FFEB3B"
            ),
            ShiftType.Work(
                id = UUID.randomUUID().toString(),
                name = "Ночная",
                startTime = LocalTime.of(20, 0),
                endTime = LocalTime.of(8, 0),
                colorHex = "#3F51B5"
            ),
            ShiftType.Off
        )
    ))
    val uiState: StateFlow<PatternSettingsUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onStartDateChange(newDate: LocalDate) {
        _uiState.update { it.copy(startDate = newDate) }
    }

    fun addShiftToPattern(shiftType: ShiftType) {
        _uiState.update { it.copy(items = it.items + shiftType) }
    }

    fun removeShiftFromPattern(index: Int) {
        _uiState.update {
            val newItems = it.items.toMutableList().apply { removeAt(index) }
            it.copy(items = newItems)
        }
    }

    fun savePattern() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.name.isNotBlank() && state.items.isNotEmpty()) {
                val pattern = ShiftPattern(
                    name = state.name,
                    items = state.items,
                    startDateEpochDay = state.startDate.toEpochDay()
                )
                saveShiftPatternUseCase(pattern)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
