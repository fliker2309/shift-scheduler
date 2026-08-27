package com.fliker.shiftscheduler.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import com.fliker.shiftscheduler.domain.usecase.GetScheduleForMonthUseCase
import com.fliker.shiftscheduler.domain.usecase.SaveShiftPatternUseCase
import com.fliker.shiftscheduler.domain.usecase.SetOverrideDayUseCase
import com.fliker.shiftscheduler.ui.calendar.CalendarScreen
import com.fliker.shiftscheduler.ui.calendar.CalendarViewModel
import com.fliker.shiftscheduler.ui.settings.PatternSettingsScreen
import com.fliker.shiftscheduler.ui.settings.PatternSettingsViewModel

@Composable
fun AppNavigation(
    repository: ShiftRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            val viewModel: CalendarViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return CalendarViewModel(
                            GetScheduleForMonthUseCase(repository),
                            SetOverrideDayUseCase(repository)
                        ) as T
                    }
                }
            )
            val state by viewModel.uiState.collectAsState()
            
            CalendarScreen(
                state = state,
                onNextMonth = { viewModel.nextMonth() },
                onPreviousMonth = { viewModel.previousMonth() },
                onOverrideClick = { date, type -> viewModel.applyOverride(date, type) },
                onClearOverride = { date -> viewModel.clearOverride(date) },
                onSettingsClick = { navController.navigate("settings") },
                availableShiftTypes = state.availableTypes
            )
        }
        
        composable("settings") {
            val viewModel: PatternSettingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return PatternSettingsViewModel(
                            SaveShiftPatternUseCase(repository)
                        ) as T
                    }
                }
            )
            val state by viewModel.uiState.collectAsState()
            
            PatternSettingsScreen(
                state = state,
                onNameChange = viewModel::onNameChange,
                onStartDateChange = viewModel::onStartDateChange,
                onAddShift = viewModel::addShiftToPattern,
                onRemoveShift = viewModel::removeShiftFromPattern,
                onSave = { 
                    viewModel.savePattern()
                    navController.popBackStack()
                }
            )
        }
    }
}
