package com.morphos.app.feature.dashboard

import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.MemoryProfile
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.model.WidgetConfig

import androidx.compose.runtime.Stable

@Stable
data class DashboardState(
    val widgets: List<WidgetConfig> = emptyList(),
    val isLoading: Boolean = true,
    val contextSnapshot: ContextSnapshot? = null,
    val memoryProfile: MemoryProfile? = null,
    val notifications: List<PrioritizedNotification> = emptyList(),
    val error: String? = null,
    val showEmptyState: Boolean = false,
    val isOffline: Boolean = false
)

sealed class DashboardIntent {
    object LoadWidgets : DashboardIntent()
    data class DeleteWidget(val widgetId: String) : DashboardIntent()
    data class PinWidgetToHomeScreen(val widgetId: String) : DashboardIntent()
    object NavigateToWidgetCreator : DashboardIntent()
    data class NavigateToWidgetEditor(val widgetId: String) : DashboardIntent()
    object RefreshAll : DashboardIntent()
    data class RecordWidgetTap(val widgetId: String) : DashboardIntent()
}

sealed class DashboardEffect {
    object NavigateToWidgetCreator : DashboardEffect()
    data class NavigateToWidgetEditor(val widgetId: String) : DashboardEffect()
    data class ShowError(val message: String) : DashboardEffect()
    data class ShowPinInstructions(val widgetId: String) : DashboardEffect()
}
