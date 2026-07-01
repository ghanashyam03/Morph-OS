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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight as GlanceFontWeight
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate
import com.morphos.app.core.widget.clickableToApp

class NotificationFeedTemplate : WidgetTemplate {
    override val templateId: String = "TPL_NOTIFICATION_FEED"
    override val displayName: String = "Notification Feed"
    override val description: String = "Lists the latest incoming smart notifications."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("notif_1_app", "notif_1_text")
    override val optionalSlots: List<String> = listOf("notif_2_app", "notif_2_text", "notif_3_app", "notif_3_text")

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val app1 = slots["notif_1_app"] ?: ""
        val txt1 = slots["notif_1_text"] ?: ""

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickableToApp()

        val list = listOfNotNull(
            getNotif(slots, 1),
            getNotif(slots, 2),
            getNotif(slots, 3)
        )

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                Box(modifier = baseModifier, contentAlignment = Alignment.CenterStart) {
                    GlanceText(
                        text = "$app1: $txt1",
                        style = GlanceTextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp),
                        maxLines = 1
                    )
                }
            }
            WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE -> {
                LazyColumn(modifier = baseModifier) {
                    items(list) { item ->
                        Row(modifier = GlanceModifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            GlanceText(
                                text = "${item.app}: ",
                                style = GlanceTextStyle(
                                    color = GlanceTheme.colors.primary,
                                    fontWeight = GlanceFontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            GlanceText(
                                text = item.text,
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
                ComposeText(text = "Notifications Feed", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                ComposeText(text = "WhatsApp: Meeting starts in 10 mins", fontSize = 12.sp)
                ComposeText(text = "Gmail: Project updates requested", fontSize = 12.sp)
            }
        }
    }

    private fun getNotif(slots: Map<String, String>, index: Int): NotifItem? {
        val app = slots["notif_${index}_app"]
        val text = slots["notif_${index}_text"]
        return if (!app.isNullOrBlank() && !text.isNullOrBlank()) {
            NotifItem(app, text)
        } else {
            null
        }
    }

    private data class NotifItem(val app: String, val text: String)
}
