package com.fliker.shiftscheduler.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "override_days")
data class OverrideDayEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val shiftTypeId: String,
    val note: String? = null
)
