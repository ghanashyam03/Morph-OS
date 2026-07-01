package com.morphos.app.core.widget.templates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight as GlanceFontWeight
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate
import com.morphos.app.core.widget.clickableToApp

class Grid2x2Template : WidgetTemplate {
    override val templateId: String = "TPL_GRID_2X2"
    override val displayName: String = "2x2 Metric Grid"
    override val description: String = "Displays a grid of four metrics with values and labels."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf(
        "metric_1_label", "metric_1_value",
        "metric_2_label", "metric_2_value",
        "metric_3_label", "metric_3_value",
        "metric_4_label", "metric_4_value"
    )
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val l1 = slots["metric_1_label"] ?: ""
        val v1 = slots["metric_1_value"] ?: ""
        val l2 = slots["metric_2_label"] ?: ""
        val v2 = slots["metric_2_value"] ?: ""
        val l3 = slots["metric_3_label"] ?: ""
        val v3 = slots["metric_3_value"] ?: ""
        val l4 = slots["metric_4_label"] ?: ""
        val v4 = slots["metric_4_value"] ?: ""

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
                    GlanceText(
                        text = v1,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val paddingVal = if (sizeClass == WidgetSizeClass.LARGE) 8.dp else 4.dp
                val valueSize = if (sizeClass == WidgetSizeClass.LARGE) 20.sp else 16.sp
                val labelSize = if (sizeClass == WidgetSizeClass.LARGE) 12.sp else 10.sp

                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = GlanceModifier.defaultWeight()) {
                        Column(modifier = GlanceModifier.defaultWeight().padding(paddingVal)) {
                            GlanceText(text = v1, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valueSize))
                            GlanceText(text = l1, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = labelSize), maxLines = 1)
                        }
                        Column(modifier = GlanceModifier.defaultWeight().padding(paddingVal)) {
                            GlanceText(text = v2, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valueSize))
                            GlanceText(text = l2, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = labelSize), maxLines = 1)
                        }
                    }
                    Row(modifier = GlanceModifier.defaultWeight()) {
                        Column(modifier = GlanceModifier.defaultWeight().padding(paddingVal)) {
                            GlanceText(text = v3, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valueSize))
                            GlanceText(text = l3, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = labelSize), maxLines = 1)
                        }
                        Column(modifier = GlanceModifier.defaultWeight().padding(paddingVal)) {
                            GlanceText(text = v4, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valueSize))
                            GlanceText(text = l4, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = labelSize), maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Preview() {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ComposeText(text = "10k", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ComposeText(text = "Steps", fontSize = 11.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ComposeText(text = "85%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ComposeText(text = "Battery", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ComposeText(text = "2", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ComposeText(text = "Alarms", fontSize = 11.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ComposeText(text = "Sunny", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        ComposeText(text = "Weather", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
