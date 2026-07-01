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

class CardDualTemplate : WidgetTemplate {
    override val templateId: String = "TPL_CARD_DUAL"
    override val displayName: String = "Dual Info Card"
    override val description: String = "Displays two columns/rows of header-body informational cards."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("header_1", "body_1", "header_2", "body_2")
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val h1 = slots["header_1"] ?: ""
        val b1 = slots["body_1"] ?: ""
        val h2 = slots["header_2"] ?: ""
        val b2 = slots["body_2"] ?: ""

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(
                        text = h1,
                        style = GlanceTextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontWeight = GlanceFontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val headerSize = if (sizeClass == WidgetSizeClass.LARGE) 16.sp else 13.sp
                val bodySize = if (sizeClass == WidgetSizeClass.LARGE) 13.sp else 11.sp

                Row(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = GlanceModifier.defaultWeight().padding(4.dp)) {
                        GlanceText(
                            text = h1,
                            style = GlanceTextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontWeight = GlanceFontWeight.Bold,
                                fontSize = headerSize
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        GlanceText(
                            text = b1,
                            style = GlanceTextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = bodySize
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Column(modifier = GlanceModifier.defaultWeight().padding(4.dp)) {
                        GlanceText(
                            text = h2,
                            style = GlanceTextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontWeight = GlanceFontWeight.Bold,
                                fontSize = headerSize
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        GlanceText(
                            text = b2,
                            style = GlanceTextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = bodySize
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Preview() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ComposeText(text = "Header 1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    ComposeText(text = "Body content 1", fontSize = 12.sp)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ComposeText(text = "Header 2", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    ComposeText(text = "Body content 2", fontSize = 12.sp)
                }
            }
        }
    }
}
