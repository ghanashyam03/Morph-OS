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

class CountdownTemplate : WidgetTemplate {
    override val templateId: String = "TPL_COUNTDOWN"
    override val displayName: String = "Countdown Timer"
    override val description: String = "Shows remaining days/hours left to a target event."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("label", "days_left")
    override val optionalSlots: List<String> = listOf("hours_left")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val label = slots["label"] ?: ""
        val days = slots["days_left"] ?: "0"
        val hours = slots["hours_left"]

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
                    GlanceText(
                        text = "${days}d",
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
            WidgetSizeClass.MEDIUM -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(text = label, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 12.sp))
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    GlanceText(
                        text = "$days days left",
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 16.sp)
                    )
                }
            }
            WidgetSizeClass.LARGE -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(text = label, style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 13.sp))
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    GlanceText(
                        text = "$days days left",
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 24.sp)
                    )
                    if (!hours.isNullOrBlank()) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        GlanceText(
                            text = "($hours hours remaining)",
                            style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontSize = 12.sp)
                        )
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
                ComposeText(text = "Final Exams", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                ComposeText(text = "14 days left", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ComposeText(text = "(334 hours remaining)", fontSize = 11.sp)
            }
        }
    }
}
