package com.fliker.shiftscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_patterns")
data class ShiftPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val patternJson: String, // Сериализованная последовательность смен (JSON)
    val startDateEpochDay: Long, // Дата старта отсчёта графика
    val isActive: Boolean = false
)
