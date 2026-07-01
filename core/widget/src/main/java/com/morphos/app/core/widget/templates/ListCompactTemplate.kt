package com.morphos.app.core.widget.templates

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate
import com.morphos.app.core.widget.clickableToApp

class ListCompactTemplate : WidgetTemplate {
    override val templateId: String = "TPL_LIST_COMPACT"
    override val displayName: String = "Compact List"
    override val description: String = "Renders up to 5 vertical bullet points."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("item_1", "item_2")
    override val optionalSlots: List<String> = listOf("item_3", "item_4", "item_5")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        val allItems = listOfNotNull(
            slots["item_1"],
            slots["item_2"],
            slots["item_3"],
            slots["item_4"],
            slots["item_5"]
        ).filter { it.isNotBlank() }

        val showCount = when (sizeClass) {
            WidgetSizeClass.SMALL -> 1
            WidgetSizeClass.MEDIUM -> minOf(3, allItems.size)
            WidgetSizeClass.LARGE -> allItems.size
        }

        val itemsToRender = allItems.take(showCount)

        Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
            itemsToRender.forEach { item ->
                Row(
                    modifier = GlanceModifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlanceText(
                        text = "• ",
                        style = GlanceTextStyle(color = GlanceTheme.colors.primary, fontSize = 14.sp)
                    )
                    GlanceText(
                        text = item,
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp),
                        maxLines = 1
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
                ComposeText(text = "• First Item", fontSize = 13.sp)
                ComposeText(text = "• Second Item", fontSize = 13.sp)
                ComposeText(text = "• Third Item", fontSize = 13.sp)
            }
        }
    }
}
