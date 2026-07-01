package com.morphos.app.core.widget

import android.app.Activity
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import com.morphos.app.core.domain.model.WidgetSizeClass

interface WidgetTemplate {
    val templateId: String
    val displayName: String
    val description: String
    val supportedSizes: List<WidgetSizeClass>
    val requiredSlots: List<String>
    val optionalSlots: List<String>

    @Composable
    fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String)

    @Composable
    fun Preview()
}

fun getMainActivityAction(): Action {
    return actionStartActivity(ComponentName("com.morphos.app", "com.morphos.app.MainActivity"))
}

fun GlanceModifier.clickableToApp(): GlanceModifier {
    return this.clickable(getMainActivityAction())
}
