package com.morphos.app.core.widget.templates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
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

class HeroProgressTemplate : WidgetTemplate {
    override val templateId: String = "TPL_HERO_PROGRESS"
    override val displayName: String = "Hero Progress Card"
    override val description: String = "Displays a goal metric with a visual text progress bar."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("title", "progress_value", "progress_max", "detail")
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val title = slots["title"] ?: ""
        val pValue = slots["progress_value"] ?: "0"
        val pMax = slots["progress_max"] ?: "100"
        val detail = slots["detail"] ?: ""

        val valFloat = pValue.toFloatOrNull() ?: 0f
        val maxFloat = pMax.toFloatOrNull() ?: 100f
        val fraction = "$pValue/$pMax"

        val filled = if (maxFloat > 0f) {
            ((valFloat / maxFloat) * 10).toInt().coerceIn(0, 10)
        } else {
            0
        }
        val progressBarText = "█".repeat(filled) + "░".repeat(10 - filled)

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
                    GlanceText(text = fraction, style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontWeight = GlanceFontWeight.Bold, fontSize = 14.sp))
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val titleSize = if (sizeClass == WidgetSizeClass.LARGE) 18.sp else 14.sp
                val barSize = if (sizeClass == WidgetSizeClass.LARGE) 18.sp else 14.sp

                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = title,
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = titleSize)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    GlanceText(
                        text = progressBarText,
                        style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontSize = barSize)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlanceText(
                            text = "$fraction — ",
                            style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 12.sp)
                        )
                        GlanceText(
                            text = detail,
                            style = GlanceTextStyle(color = GlanceTheme.colors.secondary, fontSize = 12.sp),
                            maxLines = 1
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
                ComposeText(text = "Daily Steps Goal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                ComposeText(text = "6500 / 10000 steps — 3500 remaining", fontSize = 12.sp)
            }
        }
    }
}
