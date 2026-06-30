package com.morphos.app.core.widget

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
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

@Suppress("UNCHECKED_CAST")
fun GlanceModifier.clickableToApp(): GlanceModifier {
    val mainActivityClass = try {
        Class.forName("com.morphos.app.MainActivity") as? Class<out Activity>
    } catch (e: Exception) {
        null
    }
    return if (mainActivityClass != null) {
        this.clickable(actionStartActivity(mainActivityClass))
    } else {
        this
    }
}
