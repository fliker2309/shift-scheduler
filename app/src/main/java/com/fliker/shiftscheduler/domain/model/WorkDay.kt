package com.fliker.shiftscheduler.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class WorkDay(
    val date: LocalDate,
    val shiftType: ShiftType,
    val isCustomOverride: Boolean = false,
    val note: String? = null
)
