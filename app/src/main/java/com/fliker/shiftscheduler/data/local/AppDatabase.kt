package com.fliker.shiftscheduler.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fliker.shiftscheduler.data.local.dao.ShiftDao
import com.fliker.shiftscheduler.data.local.entity.OverrideDayEntity
import com.fliker.shiftscheduler.data.local.entity.ShiftPatternEntity

@Database(
    entities = [ShiftPatternEntity::class, OverrideDayEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao
}
