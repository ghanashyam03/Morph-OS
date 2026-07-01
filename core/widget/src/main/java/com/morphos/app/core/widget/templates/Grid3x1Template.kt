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

class Grid3x1Template : WidgetTemplate {
    override val templateId: String = "TPL_GRID_3X1"
    override val displayName: String = "3x1 Horizontal Grid"
    override val description: String = "Displays three side-by-side columns of emojis and values."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf(
        "slot_1_icon", "slot_1_value",
        "slot_2_icon", "slot_2_value",
        "slot_3_icon", "slot_3_value"
    )
    override val optionalSlots: List<String> = emptyList()

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val i1 = slots["slot_1_icon"] ?: ""
        val v1 = slots["slot_1_value"] ?: ""
        val i2 = slots["slot_2_icon"] ?: ""
        val v2 = slots["slot_2_value"] ?: ""
        val i3 = slots["slot_3_icon"] ?: ""
        val v3 = slots["slot_3_value"] ?: ""

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Row(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    GlanceText(text = "$i1 $v1", style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = 12.sp))
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val valSize = if (sizeClass == WidgetSizeClass.LARGE) 16.sp else 13.sp
                val iconSize = if (sizeClass == WidgetSizeClass.LARGE) 20.sp else 16.sp

                Row(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        GlanceText(text = i1, style = GlanceTextStyle(fontSize = iconSize))
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        GlanceText(text = v1, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valSize), maxLines = 1)
                    }
                    Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        GlanceText(text = i2, style = GlanceTextStyle(fontSize = iconSize))
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        GlanceText(text = v2, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valSize), maxLines = 1)
                    }
                    Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        GlanceText(text = i3, style = GlanceTextStyle(fontSize = iconSize))
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        GlanceText(text = v3, style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontWeight = GlanceFontWeight.Bold, fontSize = valSize), maxLines = 1)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    ComposeText(text = "🔋", fontSize = 18.sp)
                    ComposeText(text = "85%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    ComposeText(text = "🌤", fontSize = 18.sp)
                    ComposeText(text = "Sunny", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    ComposeText(text = "📅", fontSize = 18.sp)
                    ComposeText(text = "10k", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
