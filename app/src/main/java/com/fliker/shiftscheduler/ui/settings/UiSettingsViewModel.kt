package com.fliker.shiftscheduler.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fliker.shiftscheduler.data.local.AppTheme
import com.fliker.shiftscheduler.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UiSettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onThemeChange(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
    }

    fun onDimWeekendsChange(dim: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDimWeekends(dim)
        }
    }

    fun onShowWeekNumbersChange(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateShowWeekNumbers(show)
        }
    }
}
