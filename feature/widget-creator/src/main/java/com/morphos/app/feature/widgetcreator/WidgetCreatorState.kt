package com.morphos.app.feature.widgetcreator

import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.domain.model.WidgetIntent
import com.morphos.app.core.domain.model.WidgetPlan
import com.morphos.app.core.domain.model.WidgetSizeClass

import androidx.compose.runtime.Stable

@Stable
data class WidgetCreatorState(
    val step: CreatorStep = CreatorStep.NL_INPUT,
    val rawInput: String = "",
    val parsedIntent: WidgetIntent? = null,
    val generatedPlan: WidgetPlan? = null,
    val selectedTemplate: String? = null,
    val selectedSize: WidgetSizeClass = WidgetSizeClass.MEDIUM,
    val widgetName: String = "",
    val isProcessing: Boolean = false,
    val processingMessage: String = "",
    val error: String? = null,
    val createdWidget: WidgetConfig? = null
)

enum class CreatorStep { NL_INPUT, PROCESSING, TEMPLATE_SELECTION, PREVIEW, DONE }

sealed class WidgetCreatorIntent {
    data class InputChanged(val text: String) : WidgetCreatorIntent()
    object SubmitInput : WidgetCreatorIntent()
    data class SelectTemplate(val templateId: String) : WidgetCreatorIntent()
    data class SelectSize(val size: WidgetSizeClass) : WidgetCreatorIntent()
    data class UpdateName(val name: String) : WidgetCreatorIntent()
    object ConfirmWidget : WidgetCreatorIntent()
    object Back : WidgetCreatorIntent()
    object Dismiss : WidgetCreatorIntent()
}

sealed class WidgetCreatorEffect {
    object NavigateToDashboard : WidgetCreatorEffect()
    data class ShowError(val message: String) : WidgetCreatorEffect()
}
