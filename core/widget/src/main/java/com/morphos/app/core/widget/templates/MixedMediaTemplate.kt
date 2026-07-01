package com.morphos.app.core.widget.templates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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

class MixedMediaTemplate : WidgetTemplate {
    override val templateId: String = "TPL_MIXED_MEDIA"
    override val displayName: String = "Mixed Media Card"
    override val description: String = "Displays a category label, large headline, and secondary body text."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("headline", "subtext", "label")
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val headline = slots["headline"] ?: ""
        val subtext = slots["subtext"] ?: ""
        val label = slots["label"] ?: ""

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
                    GlanceText(
                        text = label,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val labelSize = if (sizeClass == WidgetSizeClass.LARGE) 12.sp else 10.sp
                val headlineSize = if (sizeClass == WidgetSizeClass.LARGE) 18.sp else 14.sp
                val subtextSize = if (sizeClass == WidgetSizeClass.LARGE) 13.sp else 11.sp

                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = label.uppercase(),
                        style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontWeight = GlanceFontWeight.Bold, fontSize = labelSize)
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    GlanceText(
                        text = headline,
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = headlineSize),
                        maxLines = 2
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    GlanceText(
                        text = subtext,
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = subtextSize),
                        maxLines = 2
                    )
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
                ComposeText(text = "NEWS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(2.dp))
                ComposeText(text = "Major announcement made by MorphOS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ComposeText(text = "The adaptive JNI components are fully integrated.", fontSize = 12.sp)
            }
        }
    }
}
