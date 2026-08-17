package com.fliker.shiftscheduler.domain.model

import java.time.LocalTime

sealed class ShiftType(
    open val id: String,
    open val name: String,
    val isWorkDay: Boolean
) {
    data class Work(
        override val id: String,
        override val name: String,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val colorHex: String
    ) : ShiftType(id, name, isWorkDay = true)

    object Off : ShiftType(id = "off", name = "Выходной", isWorkDay = false)
    object Vacation : ShiftType(id = "vacation", name = "Отпуск", isWorkDay = false)
    object SickLeave : ShiftType(id = "sick", name = "Больничный", isWorkDay = false)
}