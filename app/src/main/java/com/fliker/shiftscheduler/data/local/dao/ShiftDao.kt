package com.fliker.shiftscheduler.data.local.dao

import androidx.room.*
import com.fliker.shiftscheduler.data.local.entity.OverrideDayEntity
import com.fliker.shiftscheduler.data.local.entity.ShiftPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shift_patterns WHERE isActive = 1 LIMIT 1")
    fun getActivePattern(): Flow<ShiftPatternEntity?>

    @Query("SELECT * FROM shift_patterns")
    fun getAllPatterns(): Flow<List<ShiftPatternEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: ShiftPatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatternWithReturnId(pattern: ShiftPatternEntity): Long

    @Transaction
    suspend fun setActivePattern(patternId: Long) {
        clearActiveStatus()
        updateActiveStatus(patternId, true)
    }

    @Query("UPDATE shift_patterns SET isActive = 0")
    suspend fun clearActiveStatus()

    @Query("UPDATE shift_patterns SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean)

    @Query("DELETE FROM shift_patterns WHERE id = :id")
    suspend fun deletePattern(id: Long)

    @Query("SELECT * FROM override_days WHERE dateEpochDay BETWEEN :fromEpoch AND :toEpoch")
    fun getOverrides(fromEpoch: Long, toEpoch: Long): Flow<List<OverrideDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOverride(override: OverrideDayEntity)

    @Query("DELETE FROM override_days WHERE dateEpochDay = :dateEpoch")
    suspend fun deleteOverride(dateEpoch: Long)
}
