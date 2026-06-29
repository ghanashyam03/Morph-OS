package com.morphos.app.feature.widgetcreator

import com.morphos.app.core.domain.model.WidgetPlan

sealed interface WidgetCreatorState {
    object Idle : WidgetCreatorState
    object AnalyzingInput : WidgetCreatorState
    data class PlanGenerated(val plan: WidgetPlan) : WidgetCreatorState
    object CreatingWidget : WidgetCreatorState
    data class Error(val message: String) : WidgetCreatorState
}
