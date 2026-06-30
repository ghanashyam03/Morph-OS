package com.morphos.app.core.widget.templates

import android.content.Intent
import android.net.Uri
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
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.*
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplate
import com.morphos.app.core.widget.clickableToApp

class QuickActionsTemplate : WidgetTemplate {
    override val templateId: String = "TPL_QUICK_ACTIONS"
    override val displayName: String = "Quick Action Buttons"
    override val description: String = "Provides up to 4 quick action buttons to launch features or links."
    override val supportedSizes: List<WidgetSizeClass> = listOf(WidgetSizeClass.SMALL, WidgetSizeClass.MEDIUM, WidgetSizeClass.LARGE)
    override val requiredSlots: List<String> = listOf("action_1_label", "action_1_deep_link")
    override val optionalSlots: List<String> = listOf(
        "action_2_label", "action_2_deep_link",
        "action_3_label", "action_3_deep_link",
        "action_4_label", "action_4_deep_link"
    )

    @Composable
    override fun Render(slots: Map<String, String>, sizeClass: WidgetSizeClass, widgetName: String) {
        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)

        val actions = listOfNotNull(
            getAction(slots, 1),
            getAction(slots, 2),
            getAction(slots, 3),
            getAction(slots, 4)
        )

        when (sizeClass) {
            WidgetSizeClass.SMALL -> {
                val first = actions.firstOrNull()
                Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
                    if (first != null) {
                        androidx.glance.appwidget.components.FilledButton(
                            text = first.label,
                            onClick = getClickAction(first.link)
                        )
                    }
                }
            }
            WidgetSizeClass.MEDIUM -> {
                val list = actions.take(2)
                Row(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    list.forEachIndexed { i, action ->
                        if (i > 0) Spacer(modifier = GlanceModifier.width(8.dp))
                        androidx.glance.appwidget.components.FilledButton(
                            text = action.label,
                            onClick = getClickAction(action.link)
                        )
                    }
                }
            }
            WidgetSizeClass.LARGE -> {
                Column(modifier = baseModifier, verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = GlanceModifier.padding(vertical = 4.dp)) {
                        val row1 = actions.take(2)
                        row1.forEachIndexed { i, action ->
                            if (i > 0) Spacer(modifier = GlanceModifier.width(8.dp))
                            androidx.glance.appwidget.components.FilledButton(
                                text = action.label,
                                onClick = getClickAction(action.link)
                            )
                        }
                    }
                    if (actions.size > 2) {
                        Row(modifier = GlanceModifier.padding(vertical = 4.dp)) {
                            val row2 = actions.drop(2).take(2)
                            row2.forEachIndexed { i, action ->
                                if (i > 0) Spacer(modifier = GlanceModifier.width(8.dp))
                                androidx.glance.appwidget.components.FilledButton(
                                    text = action.label,
                                    onClick = getClickAction(action.link)
                                )
                            }
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
                ComposeText(text = "Quick Actions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(onClick = {}) {
                        ComposeText("Action 1")
                    }
                    androidx.compose.material3.Button(onClick = {}) {
                        ComposeText("Action 2")
                    }
                }
            }
        }
    }

    private fun getAction(slots: Map<String, String>, index: Int): ActionItem? {
        val label = slots["action_${index}_label"]
        val link = slots["action_${index}_deep_link"]
        return if (!label.isNullOrBlank() && !link.isNullOrBlank()) {
            ActionItem(label, link)
        } else {
            null
        }
    }

    private fun getClickAction(link: String): Action {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            actionStartActivity(intent)
        } catch (e: Exception) {
            val mainActivityClass = try {
                Class.forName("com.morphos.app.MainActivity") as Class<out android.app.Activity>
            } catch (ex: Exception) {
                android.app.Activity::class.java
            }
            actionStartActivity(mainActivityClass)
        }
    }

    private data class ActionItem(val label: String, val link: String)
}
