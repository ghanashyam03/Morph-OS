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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight as GlanceFontWeight
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate
import com.morphos.app.core.widget.clickableToApp

class CardSingleTemplate : WidgetTemplate {
    override val templateId: String = "TPL_CARD_SINGLE"
    override val displayName: String = "Single Info Card"
    override val description: String = "Displays a header, body text, and an optional button."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("header", "body")
    override val optionalSlots: List<String> = listOf("action_label", "action_deep_link")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val header = slots["header"] ?: ""
        val body = slots["body"] ?: ""
        val actionLabel = slots["action_label"]

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = header,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
            WidgetSizeClass.MEDIUM -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = header,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    GlanceText(
                        text = body,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            WidgetSizeClass.LARGE -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = header,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    GlanceText(
                        text = body,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 14.sp
                        )
                    )
                    if (!actionLabel.isNullOrBlank()) {
                        Spacer(modifier = GlanceModifier.height(12.dp))
                        androidx.glance.appwidget.components.FilledButton(
                            text = actionLabel,
                            onClick = com.morphos.app.core.widget.getMainActivityAction()
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
            Column(modifier = Modifier.padding(16.dp)) {
                ComposeText(
                    text = "Header Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                ComposeText(
                    text = "This is a single card template preview describing the dynamic status.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
