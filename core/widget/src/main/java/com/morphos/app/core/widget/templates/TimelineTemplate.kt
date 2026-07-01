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

class TimelineTemplate : WidgetTemplate {
    override val templateId: String = "TPL_TIMELINE"
    override val displayName: String = "Timeline List"
    override val description: String = "Displays upcoming timeline schedule events."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("event_0_title", "event_0_time")
    override val optionalSlots: List<String> = listOf(
        "event_1_title", "event_1_time",
        "event_2_title", "event_2_time",
        "event_3_title", "event_3_time"
    )

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val events = mutableListOf<TimelineEvent>()
        getEvent(slots, 0)?.let { events.add(it) }
        getEvent(slots, 1)?.let { events.add(it) }
        getEvent(slots, 2)?.let { events.add(it) }
        getEvent(slots, 3)?.let { events.add(it) }

        androidx.glance.layout.Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(12.dp)
                .clickableToApp(),
            verticalAlignment = androidx.glance.layout.Alignment.Top
        ) {
            androidx.glance.layout.Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = androidx.glance.layout.Alignment.CenterVertically
            ) {
                androidx.glance.text.Text(
                    text = widgetName,
                    style = GlanceTextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 16.sp,
                        fontWeight = GlanceFontWeight.Bold
                    )
                )
            }
            androidx.glance.layout.Spacer(modifier = GlanceModifier.height(8.dp))

            if (events.isEmpty()) {
                androidx.glance.text.Text(
                    text = "No events scheduled",
                    style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp)
                )
            } else {
                events.forEach { event ->
                    androidx.glance.layout.Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = androidx.glance.layout.Alignment.CenterVertically
                    ) {
                        androidx.glance.text.Text(
                            text = event.time,
                            style = GlanceTextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                                fontWeight = GlanceFontWeight.Bold
                            ),
                            modifier = GlanceModifier.width(50.dp)
                        )
                        androidx.glance.text.Text(
                            text = event.title,
                            style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp)
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
                androidx.compose.foundation.layout.Row {
                    ComposeText(text = "09:00", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(60.dp), fontSize = 13.sp)
                    ComposeText(text = "Lectures on History", fontSize = 13.sp)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.foundation.layout.Row {
                    ComposeText(text = "14:00", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(60.dp), fontSize = 13.sp)
                    ComposeText(text = "Gym Workout", fontSize = 13.sp)
                }
            }
        }
    }

    private fun getEvent(slots: Map<String, String>, index: Int): TimelineEvent? {
        val title = slots["event_${index}_title"]
        val time = slots["event_${index}_time"]
        return if (!title.isNullOrBlank() && !time.isNullOrBlank()) {
            TimelineEvent(title, time)
        } else {
            null
        }
    }

    private data class TimelineEvent(val title: String, val time: String)
}
