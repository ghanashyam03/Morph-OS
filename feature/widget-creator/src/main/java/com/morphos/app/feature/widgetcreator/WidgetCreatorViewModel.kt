package com.morphos.app.feature.widgetcreator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WidgetCreatorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<WidgetCreatorState>(WidgetCreatorState.Idle)
    val uiState: StateFlow<WidgetCreatorState> = _uiState

    fun processNaturalLanguageInput(input: String) {
        TODO("Not yet implemented")
    }
}
