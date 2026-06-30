package com.morphos.app.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.morphos.app.core.data.worker.WidgetDataPrefetchWorker

class MorphOsAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MorphOsGlanceWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<WidgetDataPrefetchWorker>().build()
        workManager.enqueue(request)
    }
}
