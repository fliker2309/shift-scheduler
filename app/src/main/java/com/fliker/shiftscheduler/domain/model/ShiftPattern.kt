package com.fliker.shiftscheduler.domain.model

data class ShiftPattern(
    val id: Long = 0,
    val name: String,
    val items: List<ShiftType>,
    val durationDays: Int = items.size
)
