package com.fliker.shiftscheduler.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime

@Serializable
sealed class ShiftType(
    open val id: String,
    open val name: String,
    val isWorkDay: Boolean
) {
    @Serializable
    data class Work(
        override val id: String,
        override val name: String,
        @Serializable(with = LocalTimeSerializer::class)
        val startTime: LocalTime,
        @Serializable(with = LocalTimeSerializer::class)
        val endTime: LocalTime,
        val colorHex: String
    ) : ShiftType(id, name, isWorkDay = true)

    @Serializable
    object Off : ShiftType(id = "off", name = "Выходной", isWorkDay = false)

    @Serializable
    object Vacation : ShiftType(id = "vacation", name = "Отпуск", isWorkDay = false)

    @Serializable
    object SickLeave : ShiftType(id = "sick", name = "Больничный", isWorkDay = false)
}

object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString())
}