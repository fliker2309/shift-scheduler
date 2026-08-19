package com.fliker.shiftscheduler.data.local.dao

import androidx.room.*
import com.fliker.shiftscheduler.data.local.entity.OverrideDayEntity
import com.fliker.shiftscheduler.data.local.entity.ShiftPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shift_patterns WHERE isActive = 1 LIMIT 1")
    fun getActivePattern(): Flow<ShiftPatternEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: ShiftPatternEntity)

    @Query("SELECT * FROM override_days WHERE dateEpochDay BETWEEN :fromEpoch AND :toEpoch")
    fun getOverrides(fromEpoch: Long, toEpoch: Long): Flow<List<OverrideDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOverride(override: OverrideDayEntity)

    @Query("DELETE FROM override_days WHERE dateEpochDay = :dateEpoch")
    suspend fun deleteOverride(dateEpoch: Long)
}
