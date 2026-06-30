package com.morphos.app.core.widget.templates

import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate

class UnknownTemplate : WidgetTemplate {
    override val templateId: String = "TPL_UNKNOWN"
    override val displayName: String = "Unknown"
    override val description: String = "Fallback template when specified template ID is not found."
    override val supportedSizes: List<WidgetSizeClass> = WidgetSizeClass.values().toList()
    override val requiredSlots: List<String> = emptyList()
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        Box(
            modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            GlanceText(
                text = "Template Not Found",
                style = GlanceTextStyle(color = GlanceTheme.colors.error)
            )
        }
    }

    @Composable
    override fun Preview() {
        ElevatedCard {
            ComposeText(text = "Template Not Found")
        }
    }
}
