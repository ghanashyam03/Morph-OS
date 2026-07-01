package com.morphos.app.core.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlanceWidgetRenderer @Inject constructor(
    private val widgetUpdatePipeline: WidgetUpdatePipeline,
    @ApplicationContext private val context: Context
) {
    suspend fun renderWidget(widgetId: String) = widgetUpdatePipeline.updateWidget(widgetId)
    suspend fun renderAllWidgets() = widgetUpdatePipeline.updateAllWidgets()

    suspend fun setLoadingState(glanceId: GlanceId) {
        updateAppWidgetState(context, MorphOsWidgetStateDefinition, glanceId) { state ->
            state.copy(isLoading = true)
        }
        MorphOsGlanceWidget().update(context, glanceId)
    }

    suspend fun setErrorState(glanceId: GlanceId, error: String) {
        updateAppWidgetState(context, MorphOsWidgetStateDefinition, glanceId) { state ->
            state.copy(isLoading = false, errorMessage = error)
        }
        MorphOsGlanceWidget().update(context, glanceId)
    }
}
