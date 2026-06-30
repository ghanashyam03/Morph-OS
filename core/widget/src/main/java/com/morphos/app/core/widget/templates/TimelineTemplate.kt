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

class TimelineTemplate : WidgetTemplate {
    override val templateId: String = "TPL_TIMELINE"
    override val displayName: String = "Timeline List"
    override val description: String = "Displays upcoming timeline schedule events."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("event_1_title", "event_1_time")
    override val optionalSlots: List<String> = listOf("event_2_title", "event_2_time", "event_3_title", "event_3_time")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        val events = listOfNotNull(
            getEvent(slots, 1),
            getEvent(slots, 2),
            getEvent(slots, 3)
        )

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                val first = events.firstOrNull()
                Box(modifier = baseModifier, contentAlignment = Alignment.CenterStart) {
                    if (first != null) {
                        GlanceText(
                            text = "${first.time} ${first.title}",
                            style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp),
                            maxLines = 1
                        )
                    }
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                val maxEvents = if (sizeClass == WidgetSizeClass.LARGE) 3 else 2
                val eventsToRender = events.take(maxEvents)

                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically) {
                    eventsToRender.forEach { ev ->
                        Row(modifier = GlanceModifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            GlanceText(
                                text = ev.time,
                                style = GlanceTextStyle(
                                    color = GlanceTheme.colors.primary,
                                    fontWeight = GlanceFontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                modifier = GlanceModifier.width(60.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            GlanceText(
                                text = ev.title,
                                style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp),
                                maxLines = 1
                            )
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
                Row {
                    ComposeText(text = "09:00", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(60.dp), fontSize = 13.sp)
                    ComposeText(text = "Lectures on History", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
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
