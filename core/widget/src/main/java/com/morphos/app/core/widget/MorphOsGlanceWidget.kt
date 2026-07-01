package com.morphos.app.core.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.morphos.app.core.domain.model.WidgetSizeClass

class MorphOsGlanceWidget : GlanceAppWidget() {
    override val stateDefinition = MorphOsWidgetStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 40.dp),   // SMALL: 2×1
            DpSize(250.dp, 110.dp),  // MEDIUM: 4×2
            DpSize(250.dp, 250.dp)   // LARGE: 4×4
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<MorphOsWidgetState>()
            val size = LocalSize.current

            val sizeClass = when {
                size.width < 200.dp -> WidgetSizeClass.SMALL
                size.height < 100.dp -> WidgetSizeClass.SMALL
                size.height < 200.dp -> WidgetSizeClass.MEDIUM
                else -> WidgetSizeClass.LARGE
            }

            GlanceTheme {
                if (state.isLoading) {
                    WidgetLoadingContent()
                } else if (state.errorMessage != null) {
                    WidgetErrorContent(message = state.errorMessage)
                } else {
                    WidgetTemplateRegistry.render(
                        templateId = state.templateId,
                        slots = state.resolvedSlots,
                        sizeClass = sizeClass,
                        widgetName = state.widgetName
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetLoadingContent() {
    Box(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(color = GlanceTheme.colors.onBackground)
        )
    }
}

@Composable
fun WidgetErrorContent(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠",
            style = TextStyle(fontSize = 24.sp, color = GlanceTheme.colors.error)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = message,
            maxLines = 2,
            style = TextStyle(color = GlanceTheme.colors.onErrorContainer)
        )
    }
}
