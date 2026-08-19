package com.fliker.shiftscheduler.ui.settings

import com.fliker.shiftscheduler.domain.model.ShiftType
import java.time.LocalDate

data class PatternSettingsUiState(
    val name: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val items: List<ShiftType> = emptyList(),
    val availableTypes: List<ShiftType> = emptyList(),
    val isSaved: Boolean = false
)
