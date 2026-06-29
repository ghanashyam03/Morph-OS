package com.morphos.app.feature.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<String>("Onboarding State Placeholder")
    val uiState: StateFlow<String> = _uiState
}
