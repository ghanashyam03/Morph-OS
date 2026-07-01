package com.morphos.app.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.morphos.app.core.data.worker.WidgetDataPrefetchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class MorphOsAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MorphOsGlanceWidget()
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        receiverScope.launch {
                val pending = context.getSharedPreferences(WidgetPinning.PREFERENCES, Context.MODE_PRIVATE)
                appWidgetIds.forEach { appWidgetId ->
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    val widgetId = options.getString(WidgetPinning.OPTION_WIDGET_ID)
                        ?: pending.getString(WidgetPinning.OPTION_WIDGET_ID, null)
                        ?: return@forEach
                    val name = options.getString(WidgetPinning.OPTION_WIDGET_NAME)
                        ?: pending.getString(WidgetPinning.OPTION_WIDGET_NAME, null)
                        ?: "MorphOS Widget"
                    val templateId = options.getString(WidgetPinning.OPTION_TEMPLATE_ID)
                        ?: pending.getString(WidgetPinning.OPTION_TEMPLATE_ID, null)
                        ?: "TPL_CARD_SINGLE"
                    val slotsJson = options.getString(WidgetPinning.OPTION_SLOTS)
                        ?: pending.getString(WidgetPinning.OPTION_SLOTS, null)
                    val slots = slotsJson?.let {
                        runCatching { Json.decodeFromString<Map<String, String>>(it) }.getOrNull()
                    }.orEmpty()
                    WidgetBindingStore(context).bind(appWidgetId, widgetId)
                    val glanceId = AppWidgetId(appWidgetId)
                    updateAppWidgetState(context, MorphOsWidgetStateDefinition, glanceId) {
                        MorphOsWidgetState(
                            widgetId = widgetId,
                            widgetName = name,
                            templateId = templateId,
                            resolvedSlots = slots,
                            isLoading = false,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    MorphOsGlanceWidget().update(context, glanceId)
                    EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        WidgetPipelineEntryPoint::class.java
                    ).pipeline().updateWidget(widgetId)
                    pending.edit().clear().apply()
                }
        }
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<WidgetDataPrefetchWorker>().build()
        workManager.enqueue(request)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetPipelineEntryPoint {
    fun pipeline(): WidgetUpdatePipeline
}

internal class WidgetBindingStore(context: Context) {
    private val preferences = context.getSharedPreferences("widget_bindings", Context.MODE_PRIVATE)

    fun bind(appWidgetId: Int, widgetId: String) {
        preferences.edit().putString(appWidgetId.toString(), widgetId).apply()
    }

    fun appWidgetIdsFor(widgetId: String): Set<Int> = preferences.all.mapNotNull { (key, value) ->
        key.toIntOrNull()?.takeIf { value == widgetId }
    }.toSet()
}
