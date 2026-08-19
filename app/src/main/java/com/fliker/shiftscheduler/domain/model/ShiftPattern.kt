package com.fliker.shiftscheduler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShiftPattern(
    val id: Long = 0,
    val name: String,
    val items: List<ShiftType>,
    val startDateEpochDay: Long = 0 // Adding this to domain model as well for easier mapping
) {
    val durationDays: Int = items.size
}
