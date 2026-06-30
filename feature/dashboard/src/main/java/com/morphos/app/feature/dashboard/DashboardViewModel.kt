package com.morphos.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.NoParams
import com.morphos.app.core.domain.agent.AgentOrchestrator
import com.morphos.app.core.domain.model.ShortTermEvent
import com.morphos.app.core.domain.model.ShortTermEventType
import com.morphos.app.core.domain.usecase.widget.DeleteWidgetUseCase
import com.morphos.app.core.domain.usecase.widget.GetAllWidgetsUseCase
import com.morphos.app.core.domain.usecase.widget.RecordUserEventUseCase
import com.morphos.app.core.widget.GlanceWidgetRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAllWidgetsUseCase: GetAllWidgetsUseCase,
    private val deleteWidgetUseCase: DeleteWidgetUseCase,
    private val recordUserEventUseCase: RecordUserEventUseCase,
    private val agentOrchestrator: AgentOrchestrator,
    private val glanceWidgetRenderer: GlanceWidgetRenderer,
    private val connectivityObserver: com.morphos.app.core.common.ConnectivityObserver,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<DashboardEffect> = _effects.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch(dispatchers.main) {
            _effects.emit(DashboardEffect.ShowError(throwable.message ?: "Unknown error occurred"))
        }
    }

    init {
        processIntent(DashboardIntent.LoadWidgets)
        
        // Observe context changes
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            agentOrchestrator.getContextFlow().collect { ctx ->
                _state.update { it.copy(contextSnapshot = ctx) }
            }
        }
        
        // Observe notifications
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            agentOrchestrator.getNotificationFlow().collect { notifs ->
                _state.update { it.copy(notifications = notifs) }
            }
        }

        // Observe connectivity
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            connectivityObserver.observe().collect { status ->
                val offline = status != com.morphos.app.core.common.ConnectivityObserver.Status.Available
                _state.update { it.copy(isOffline = offline) }
            }
        }
    }

    fun processIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.LoadWidgets -> loadWidgets()
            is DashboardIntent.DeleteWidget -> deleteWidget(intent.widgetId)
            is DashboardIntent.PinWidgetToHomeScreen -> pinWidget(intent.widgetId)
            DashboardIntent.RefreshAll -> refreshAll()
            is DashboardIntent.RecordWidgetTap -> recordTap(intent.widgetId)
            DashboardIntent.NavigateToWidgetCreator -> viewModelScope.launch(dispatchers.main + exceptionHandler) {
                _effects.emit(DashboardEffect.NavigateToWidgetCreator)
            }
            is DashboardIntent.NavigateToWidgetEditor -> viewModelScope.launch(dispatchers.main + exceptionHandler) {
                _effects.emit(DashboardEffect.NavigateToWidgetEditor(intent.widgetId))
            }
        }
    }

    private fun loadWidgets() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            getAllWidgetsUseCase(NoParams).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                widgets = result.data,
                                isLoading = false,
                                showEmptyState = result.data.isEmpty()
                            )
                        }
                    }
                    is AppResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    AppResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun deleteWidget(widgetId: String) {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            deleteWidgetUseCase(widgetId)
        }
    }

    private fun pinWidget(widgetId: String) {
        viewModelScope.launch(dispatchers.main + exceptionHandler) {
            _effects.emit(DashboardEffect.ShowPinInstructions(widgetId))
        }
    }

    private fun refreshAll() {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            glanceWidgetRenderer.renderAllWidgets()
        }
    }

    private fun recordTap(widgetId: String) {
        viewModelScope.launch(dispatchers.io + exceptionHandler) {
            recordUserEventUseCase(
                ShortTermEvent(
                    id = UUID.randomUUID().toString(),
                    eventType = ShortTermEventType.WIDGET_TAPPED,
                    widgetId = widgetId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
