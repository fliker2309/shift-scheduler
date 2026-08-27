package com.fliker.shiftscheduler.data.repository

import com.fliker.shiftscheduler.data.local.dao.ShiftDao
import com.fliker.shiftscheduler.data.local.entity.OverrideDayEntity
import com.fliker.shiftscheduler.data.local.entity.ShiftPatternEntity
import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class ShiftRepositoryImpl(
    private val shiftDao: ShiftDao
) : ShiftRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getActivePattern(): Flow<ShiftPattern?> =
        shiftDao.getActivePattern().map { entity ->
            entity?.let {
                ShiftPattern(
                    id = it.id,
                    name = it.name,
                    items = json.decodeFromString(it.patternJson),
                    startDateEpochDay = it.startDateEpochDay
                )
            }
        }

    override fun getAllPatterns(): Flow<List<ShiftPattern>> =
        shiftDao.getAllPatterns().map { entities ->
            entities.map { entity ->
                ShiftPattern(
                    id = entity.id,
                    name = entity.name,
                    items = json.decodeFromString(entity.patternJson),
                    startDateEpochDay = entity.startDateEpochDay
                )
            }
        }

    override suspend fun savePattern(pattern: ShiftPattern) {
        val entity = ShiftPatternEntity(
            id = pattern.id,
            name = pattern.name,
            patternJson = json.encodeToString(pattern.items),
            startDateEpochDay = pattern.startDateEpochDay,
            isActive = true
        )
        if (entity.isActive) {
            shiftDao.setActivePattern(shiftDao.insertPatternWithReturnId(entity))
        } else {
            shiftDao.insertPattern(entity)
        }
    }

    override suspend fun setActivePattern(patternId: Long) {
        shiftDao.setActivePattern(patternId)
    }

    override suspend fun deletePattern(patternId: Long) {
        shiftDao.deletePattern(patternId)
    }

    override fun getCustomOverrides(from: LocalDate, to: LocalDate): Flow<List<WorkDay>> {
        return combine(
            shiftDao.getOverrides(from.toEpochDay(), to.toEpochDay()),
            getActivePattern()
        ) { overrides, activePattern ->
            overrides.map { entity ->
                WorkDay(
                    date = LocalDate.ofEpochDay(entity.dateEpochDay),
                    shiftType = findShiftType(entity.shiftTypeId, activePattern),
                    isCustomOverride = true,
                    note = entity.note
                )
            }
        }
    }

    override suspend fun saveCustomOverride(workDay: WorkDay) {
        shiftDao.upsertOverride(
            OverrideDayEntity(
                dateEpochDay = workDay.date.toEpochDay(),
                shiftTypeId = workDay.shiftType.id,
                note = workDay.note
            )
        )
    }

    override suspend fun deleteCustomOverride(date: LocalDate) {
        shiftDao.deleteOverride(date.toEpochDay())
    }

    private fun findShiftType(id: String, activePattern: ShiftPattern?): ShiftType {
        return when (id) {
            ShiftType.Off.id -> ShiftType.Off
            ShiftType.Vacation.id -> ShiftType.Vacation
            ShiftType.SickLeave.id -> ShiftType.SickLeave
            else -> activePattern?.items?.find { it.id == id } ?: ShiftType.Off
        }
    }
}
