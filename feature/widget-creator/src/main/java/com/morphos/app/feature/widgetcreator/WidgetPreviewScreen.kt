package com.morphos.app.feature.widgetcreator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morphos.app.core.domain.model.WidgetSizeClass
import com.morphos.app.core.widget.WidgetTemplateRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPreviewScreen(
    state: WidgetCreatorState,
    onIntent: (WidgetCreatorIntent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview Widget", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Widget Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Simulated Home Screen box container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF3E2D77), Color(0xFF161036))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Adjust size constraint based on selection
                val modifier = when (state.selectedSize) {
                    WidgetSizeClass.SMALL -> Modifier.size(140.dp, 80.dp)
                    WidgetSizeClass.MEDIUM -> Modifier.fillMaxWidth().height(100.dp)
                    WidgetSizeClass.LARGE -> Modifier.fillMaxSize()
                }

                Box(
                    modifier = modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.widgetName.ifBlank { "Custom Widget" },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            state.generatedPlan?.slotAssignments?.values?.distinct()
                                ?.joinToString(" • ") { it.replace('_', ' ').replaceFirstChar(Char::uppercase) }
                                .orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            state.selectedTemplate?.removePrefix("TPL_")?.replace('_', ' ').orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Widget Name text field
            OutlinedTextField(
                value = state.widgetName,
                onValueChange = { onIntent(WidgetCreatorIntent.UpdateName(it)) },
                label = { Text("Widget Name") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Widget Size class selector
            Text(
                text = "Widget Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WidgetSizeClass.values().forEach { size ->
                    val isSelected = state.selectedSize == size
                    FilterChip(
                        selected = isSelected,
                        onClick = { onIntent(WidgetCreatorIntent.SelectSize(size)) },
                        label = { Text(size.name) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onIntent(WidgetCreatorIntent.ConfirmWidget) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Create Widget")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Change Template")
            }
        }
    }
}
