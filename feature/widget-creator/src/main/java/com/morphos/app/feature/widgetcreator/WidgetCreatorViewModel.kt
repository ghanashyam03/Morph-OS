package com.morphos.app.feature.widgetcreator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.domain.usecase.widget.CreateWidgetParams
import com.morphos.app.core.domain.usecase.widget.CreateWidgetUseCase
import com.morphos.app.core.domain.usecase.widget.GenerateWidgetPlanUseCase
import com.morphos.app.core.domain.usecase.widget.ParseUserIntentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetCreatorViewModel @Inject constructor(
    private val parseUserIntentUseCase: ParseUserIntentUseCase,
    private val generateWidgetPlanUseCase: GenerateWidgetPlanUseCase,
    private val createWidgetUseCase: CreateWidgetUseCase,
    private val agentOrchestrator: AgentOrchestrator,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(WidgetCreatorState())
    val state: StateFlow<WidgetCreatorState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<WidgetCreatorEffect>(extraBufferCapacity = 2)
    val effects: SharedFlow<WidgetCreatorEffect> = _effects.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.update { it.copy(isProcessing = false) }
        viewModelScope.launch {
            _effects.emit(WidgetCreatorEffect.ShowError(throwable.message ?: "Unknown error occurred"))
        }
    }

    fun processIntent(intent: WidgetCreatorIntent) {
        when (intent) {
            is WidgetCreatorIntent.InputChanged -> _state.update { it.copy(rawInput = intent.text, error = null) }
            WidgetCreatorIntent.SubmitInput -> submitInput()
            is WidgetCreatorIntent.SelectTemplate -> _state.update {
                it.copy(selectedTemplate = intent.templateId, step = CreatorStep.PREVIEW)
            }
            is WidgetCreatorIntent.SelectSize -> _state.update { it.copy(selectedSize = intent.size) }
            is WidgetCreatorIntent.UpdateName -> _state.update { it.copy(widgetName = intent.name) }
            WidgetCreatorIntent.ConfirmWidget -> confirmWidget()
            WidgetCreatorIntent.Back -> handleBack()
            WidgetCreatorIntent.Dismiss -> viewModelScope.launch(dispatchers.main + exceptionHandler) {
                _effects.emit(WidgetCreatorEffect.NavigateToDashboard)
            }
        }
    }

    private fun submitInput() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            val input = _state.value.rawInput.trim()
            if (input.isBlank()) {
                _state.update { it.copy(error = "Please describe the widget you want") }
                return@launch
            }
            _state.update {
                it.copy(
                    step = CreatorStep.PROCESSING,
                    isProcessing = true,
                    processingMessage = "Understanding your request..."
                )
            }

            val result = agentOrchestrator.handleUserInput(input)
            when (result) {
                is AppResult.Success -> {
                    val plan = result.data
                    _state.update {
                        it.copy(
                            generatedPlan = plan,
                            selectedTemplate = plan.selectedTemplateId,
                            widgetName = plan.suggestedName,
                            isProcessing = false,
                            step = if (plan.planConfidence > 0.75f) CreatorStep.PREVIEW else CreatorStep.TEMPLATE_SELECTION
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = result.message,
                            step = CreatorStep.NL_INPUT
                        )
                    }
                }
                AppResult.Loading -> {
                    // Handled above
                }
            }
        }
    }

    private fun confirmWidget() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            val state = _state.value
            val plan = state.generatedPlan ?: return@launch
            val finalPlan = plan.copy(
                selectedTemplateId = state.selectedTemplate ?: plan.selectedTemplateId,
                suggestedName = state.widgetName
            )
            _state.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Creating widget..."
                )
            }
            val createResult = createWidgetUseCase(CreateWidgetParams(finalPlan, state.selectedSize))
            when (createResult) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            createdWidget = createResult.data,
                            isProcessing = false,
                            step = CreatorStep.DONE
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isProcessing = false) }
                    _effects.emit(WidgetCreatorEffect.ShowError(createResult.message ?: "Failed to create widget"))
                }
                AppResult.Loading -> {
                    // Handled above
                }
            }
        }
    }

    private fun handleBack() {
        _state.update {
            it.copy(
                step = when (it.step) {
                    CreatorStep.PROCESSING -> CreatorStep.NL_INPUT
                    CreatorStep.TEMPLATE_SELECTION -> CreatorStep.NL_INPUT
                    CreatorStep.PREVIEW -> {
                        if ((it.generatedPlan?.planConfidence ?: 0f) > 0.75f) {
                            CreatorStep.NL_INPUT
                        } else {
                            CreatorStep.TEMPLATE_SELECTION
                        }
                    }
                    else -> it.step
                }
            )
        }
    }
}
