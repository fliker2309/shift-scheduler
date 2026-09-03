package com.fliker.shiftscheduler.domain.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime

@Stable
@Serializable
sealed class ShiftType {
    abstract val id: String
    abstract val name: String
    abstract val isWorkDay: Boolean

    @Serializable
    data class Work(
        override val id: String,
        override val name: String,
        @Serializable(with = LocalTimeSerializer::class)
        val startTime: LocalTime,
        @Serializable(with = LocalTimeSerializer::class)
        val endTime: LocalTime,
        val colorHex: String
    ) : ShiftType() {
        override val isWorkDay: Boolean = true
        val colorInt: Int by lazy { android.graphics.Color.parseColor(colorHex) }
    }

    @Serializable
    object Off : ShiftType() {
        override val id: String = "off"
        override val name: String = "Выходной"
        override val isWorkDay: Boolean = false
    }

    @Serializable
    object Vacation : ShiftType() {
        override val id: String = "vacation"
        override val name: String = "Отпуск"
        override val isWorkDay: Boolean = false
    }

    @Serializable
    object SickLeave : ShiftType() {
        override val id: String = "sick"
        override val name: String = "Больничный"
        override val isWorkDay: Boolean = false
    }
}

object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString())
}
