package com.morphos.app.core.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.morphos.app.core.domain.model.WidgetConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WidgetPinning {
    const val PREFERENCES = "widget_pin_pending"
    const val OPTION_WIDGET_ID = "morphos_widget_id"
    const val OPTION_WIDGET_NAME = "morphos_widget_name"
    const val OPTION_TEMPLATE_ID = "morphos_template_id"
    const val OPTION_SLOTS = "morphos_slots"

    fun request(context: Context, widget: WidgetConfig): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return false
        val slotsJson = Json.encodeToString(widget.slots.mapValues { (_, value) -> value.fallbackValue })
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString(OPTION_WIDGET_ID, widget.id)
            .putString(OPTION_WIDGET_NAME, widget.name)
            .putString(OPTION_TEMPLATE_ID, widget.templateId)
            .putString(OPTION_SLOTS, slotsJson)
            .apply()
        val options = Bundle().apply {
            putString(OPTION_WIDGET_ID, widget.id)
            putString(OPTION_WIDGET_NAME, widget.name)
            putString(OPTION_TEMPLATE_ID, widget.templateId)
            putString(
                OPTION_SLOTS,
                slotsJson
            )
        }
        return manager.requestPinAppWidget(
            ComponentName(context, MorphOsAppWidgetProvider::class.java),
            options,
            null
        )
    }
}
