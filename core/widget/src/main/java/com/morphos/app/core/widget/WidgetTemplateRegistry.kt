package com.morphos.app.core.widget

import androidx.compose.runtime.Composable
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.templates.*

object WidgetTemplateRegistry {
    private val templates: Map<String, WidgetTemplate> = mapOf(
        "TPL_CARD_SINGLE" to CardSingleTemplate(),
        "TPL_CARD_DUAL" to CardDualTemplate(),
        "TPL_LIST_COMPACT" to ListCompactTemplate(),
        "TPL_GRID_2X2" to Grid2x2Template(),
        "TPL_GRID_3X1" to Grid3x1Template(),
        "TPL_HERO_PROGRESS" to HeroProgressTemplate(),
        "TPL_TIMELINE" to TimelineTemplate(),
        "TPL_WEATHER_FOCUS" to WeatherFocusTemplate(),
        "TPL_MIXED_MEDIA" to MixedMediaTemplate(),
        "TPL_COUNTDOWN" to CountdownTemplate(),
        "TPL_NOTIFICATION_FEED" to NotificationFeedTemplate(),
        "TPL_QUICK_ACTIONS" to QuickActionsTemplate()
    )

    @Composable
    fun render(
        templateId: String,
        slots: Map<String, String>,
        sizeClass: WidgetSizeClass,
        widgetName: String
    ) {
        val template = templates[templateId] ?: UnknownTemplate()
        template.Render(slots = slots, sizeClass = sizeClass, widgetName = widgetName)
    }

    fun getAllTemplates(): List<WidgetTemplate> = templates.values.toList()
    fun getTemplate(id: String): WidgetTemplate? = templates[id]
}
