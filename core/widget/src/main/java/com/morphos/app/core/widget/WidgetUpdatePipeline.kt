package com.morphos.app.core.widget

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdatePipeline @Inject constructor(
    private val renderer: GlanceWidgetRenderer
) {
    suspend fun triggerUpdate(widgetId: String) {
        TODO("Not yet implemented")
    }
}
