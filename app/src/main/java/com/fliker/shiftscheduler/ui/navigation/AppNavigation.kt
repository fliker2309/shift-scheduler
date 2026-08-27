package com.fliker.shiftscheduler.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
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
import com.fliker.shiftscheduler.data.local.AppTheme
import com.fliker.shiftscheduler.data.local.UserPreferencesRepository
import com.fliker.shiftscheduler.domain.repository.ShiftRepository
import com.fliker.shiftscheduler.domain.usecase.*
import com.fliker.shiftscheduler.ui.calendar.CalendarScreen
import com.fliker.shiftscheduler.ui.calendar.CalendarViewModel
import com.fliker.shiftscheduler.ui.settings.*
import com.fliker.shiftscheduler.ui.theme.ShiftSchedulerTheme

@Composable
fun AppNavigation(
    repository: ShiftRepository,
    userPreferencesRepository: UserPreferencesRepository,
    navController: NavHostController = rememberNavController()
) {
    val uiSettingsViewModel: UiSettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UiSettingsViewModel(userPreferencesRepository) as T
            }
        }
    )
    val uiPrefs by uiSettingsViewModel.uiState.collectAsState()

    val darkTheme = when (uiPrefs?.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        else -> isSystemInDarkTheme()
    }

    ShiftSchedulerTheme(darkTheme = darkTheme) {
        NavHost(navController = navController, startDestination = "calendar") {
            composable("calendar") {
                val viewModel: CalendarViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CalendarViewModel(
                                GetScheduleForMonthUseCase(repository),
                                SetOverrideDayUseCase(repository),
                                GetShiftPatternsUseCase(repository),
                                SelectActivePatternUseCase(repository),
                                DeleteShiftPatternUseCase(repository)
                            ) as T
                        }
                    }
                )
                val state by viewModel.uiState.collectAsState()

                CalendarScreen(
                    state = state,
                    preferences = uiPrefs,
                    onNextMonth = { viewModel.nextMonth() },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onOverrideClick = { date, type -> viewModel.applyOverride(date, type) },
                    onClearOverride = { date -> viewModel.clearOverride(date) },
                    onSettingsClick = { navController.navigate("settings_pattern") },
                    onUiSettingsClick = { navController.navigate("settings_ui") },
                    onSelectPattern = { id -> viewModel.selectPattern(id) },
                    onDeletePattern = { id -> viewModel.deletePattern(id) },
                    availableShiftTypes = state.availableTypes
                )
            }

            composable("settings_pattern") {
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

            composable("settings_ui") {
                SettingsScreen(
                    state = uiPrefs,
                    onThemeChange = uiSettingsViewModel::onThemeChange,
                    onDimWeekendsChange = uiSettingsViewModel::onDimWeekendsChange,
                    onShowWeekNumbersChange = uiSettingsViewModel::onShowWeekNumbersChange,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
