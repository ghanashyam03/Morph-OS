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

class WeatherFocusTemplate : WidgetTemplate {
    override val templateId: String = "TPL_WEATHER_FOCUS"
    override val displayName: String = "Weather Focus"
    override val description: String = "Displays detailed weather forecasts and metrics."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("temperature", "condition", "wind_speed")
    override val optionalSlots: List<String> = listOf("high_temp", "low_temp", "condition_icon")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val temp = slots["temperature"] ?: "--"
        val cond = slots["condition"] ?: ""
        val wind = slots["wind_speed"] ?: "--"
        val high = slots["high_temp"]
        val low = slots["low_temp"]
        val icon = slots["condition_icon"] ?: "🌤"

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Row(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(text = icon, style = GlanceTextStyle(fontSize = 18.sp))
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    GlanceText(
                        text = "$temp°",
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 14.sp)
                    )
                }
            }
            WidgetSizeClass.MEDIUM -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlanceText(text = icon, style = GlanceTextStyle(fontSize = 24.sp))
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        GlanceText(
                            text = "$temp°C",
                            style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 18.sp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    GlanceText(text = cond, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp))
                    GlanceText(text = "Wind: $wind km/h", style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 11.sp))
                }
            }
            WidgetSizeClass.LARGE -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlanceText(text = icon, style = GlanceTextStyle(fontSize = 32.sp))
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        GlanceText(
                            text = "$temp°C",
                            style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 24.sp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    GlanceText(text = cond, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 14.sp))
                    GlanceText(text = "Wind: $wind km/h", style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 12.sp))

                    if (!high.isNullOrBlank() || !low.isNullOrBlank()) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!high.isNullOrBlank()) {
                                GlanceText(text = "H: $high°  ", style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontSize = 12.sp))
                            }
                            if (!low.isNullOrBlank()) {
                                GlanceText(text = "L: $low°", style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 12.sp))
                            }
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
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    ComposeText(text = "🌤", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    ComposeText(text = "23°C", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                ComposeText(text = "Mostly Sunny", fontSize = 13.sp)
                ComposeText(text = "Wind: 12 km/h", fontSize = 11.sp)
            }
        }
    }
}
