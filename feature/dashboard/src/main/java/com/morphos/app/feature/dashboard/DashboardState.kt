package com.morphos.app.feature.dashboard

import com.morphos.app.core.domain.model.WidgetConfig

sealed interface DashboardState {
    object Loading : DashboardState
    data class Success(val widgets: List<WidgetConfig>) : DashboardState
    data class Error(val message: String) : DashboardState
}

sealed interface DashboardIntent {
    object LoadWidgets : DashboardIntent
    data class DeleteWidget(val id: String) : DashboardIntent
}

sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
}
