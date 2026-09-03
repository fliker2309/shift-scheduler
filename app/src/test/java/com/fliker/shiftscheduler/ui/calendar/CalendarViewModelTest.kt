package com.fliker.shiftscheduler.ui.calendar

import com.fliker.shiftscheduler.domain.model.ShiftPattern
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import com.fliker.shiftscheduler.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeShiftRepository : ShiftRepository {
        override fun getActivePattern(): Flow<ShiftPattern?> = flowOf(null)
        override fun getAllPatterns(): Flow<List<ShiftPattern>> = flowOf(emptyList())
        override suspend fun savePattern(pattern: ShiftPattern) {}
        override suspend fun setActivePattern(patternId: Long) {}
        override suspend fun deletePattern(patternId: Long) {}
        override fun getCustomOverrides(from: LocalDate, to: LocalDate): Flow<List<WorkDay>> = flowOf(emptyList())
        override suspend fun saveCustomOverride(workDay: WorkDay) {}
        override suspend fun deleteCustomOverride(date: LocalDate) {}
    }

    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val repository = FakeShiftRepository()
        viewModel = CalendarViewModel(
            GetScheduleForMonthUseCase(repository),
            SetOverrideDayUseCase(repository),
            GetShiftPatternsUseCase(repository),
            SelectActivePatternUseCase(repository),
            DeleteShiftPatternUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test next month navigation`() {
        val initialMonth = viewModel.uiState.value.yearMonth
        viewModel.nextMonth()
        assertEquals(initialMonth.plusMonths(1), viewModel.uiState.value.yearMonth)
    }

    @Test
    fun `test previous month navigation`() {
        val initialMonth = viewModel.uiState.value.yearMonth
        viewModel.previousMonth()
        assertEquals(initialMonth.minusMonths(1), viewModel.uiState.value.yearMonth)
    }
}
