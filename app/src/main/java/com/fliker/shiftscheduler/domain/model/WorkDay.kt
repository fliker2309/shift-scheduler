package com.fliker.shiftscheduler.domain.model

import java.time.LocalDate

data class WorkDay(
    val date: LocalDate,
    val shiftType: ShiftType,
    val isCustomOverride: Boolean = false,
    val note: String? = null
)
