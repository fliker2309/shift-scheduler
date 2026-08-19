package com.fliker.shiftscheduler.domain.usecase

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetScheduleForMonthUseCaseTest {

    private val workShift = ShiftType.Work("day", "Day", java.time.LocalTime.of(8, 0), java.time.LocalTime.of(20, 0), "#FF0000")
    private val offShift = ShiftType.Off

    private class FakeShiftRepository(
        var pattern: ShiftPattern? = null,
        var overrides: List<WorkDay> = emptyList()
    ) : ShiftRepository {
        override fun getActivePattern(): Flow<ShiftPattern?> = flowOf(pattern)
        override suspend fun savePattern(pattern: ShiftPattern) { this.pattern = pattern }
        override fun getCustomOverrides(from: LocalDate, to: LocalDate): Flow<List<WorkDay>> = flowOf(overrides)
        override suspend fun saveCustomOverride(workDay: WorkDay) {}
        override suspend fun deleteCustomOverride(date: LocalDate) {}
    }

    @Test
    fun `test 2-2 pattern calculation`() = runBlocking {
        val startDate = LocalDate.of(2024, 1, 1) // Monday
        val pattern = ShiftPattern(
            name = "2/2",
            items = listOf(workShift, workShift, offShift, offShift),
            startDateEpochDay = startDate.toEpochDay()
        )
        val repository = FakeShiftRepository(pattern = pattern)
        val useCase = GetScheduleForMonthUseCase(repository)

        val schedule = useCase(startDate, startDate.plusDays(3)).first()

        assertEquals(4, schedule.size)
        assertEquals(workShift, schedule[0].shiftType) // Jan 1
        assertEquals(workShift, schedule[1].shiftType) // Jan 2
        assertEquals(offShift, schedule[2].shiftType)  // Jan 3
        assertEquals(offShift, schedule[3].shiftType)  // Jan 4
    }

    @Test
    fun `test calculation before start date`() = runBlocking {
        val startDate = LocalDate.of(2024, 1, 5) // Friday
        val pattern = ShiftPattern(
            name = "2/2",
            items = listOf(workShift, workShift, offShift, offShift),
            startDateEpochDay = startDate.toEpochDay()
        )
        val repository = FakeShiftRepository(pattern = pattern)
        val useCase = GetScheduleForMonthUseCase(repository)

        // Jan 4 should be offShift (index -1 mod 4 = 3)
        // Jan 3 should be offShift (index -2 mod 4 = 2)
        val schedule = useCase(startDate.minusDays(2), startDate.minusDays(1)).first()

        assertEquals(2, schedule.size)
        assertEquals(offShift, schedule[0].shiftType) // Jan 3
        assertEquals(offShift, schedule[1].shiftType) // Jan 4
    }

    @Test
    fun `test manual override priority`() = runBlocking {
        val startDate = LocalDate.of(2024, 1, 1)
        val pattern = ShiftPattern(
            name = "2/2",
            items = listOf(workShift, workShift, offShift, offShift),
            startDateEpochDay = startDate.toEpochDay()
        )
        
        val vacationDay = LocalDate.of(2024, 1, 2)
        val overrides = listOf(
            WorkDay(vacationDay, ShiftType.Vacation, isCustomOverride = true)
        )
        
        val repository = FakeShiftRepository(pattern = pattern, overrides = overrides)
        val useCase = GetScheduleForMonthUseCase(repository)

        val schedule = useCase(startDate, startDate.plusDays(1)).first()

        assertEquals(2, schedule.size)
        assertEquals(workShift, schedule[0].shiftType)
        assertEquals(ShiftType.Vacation, schedule[1].shiftType) // Overridden
    }
}
