package com.morphos.app.feature.widgetcreator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morphos.app.core.widget.WidgetPinning

@Composable
fun WidgetCreatorScreen(
    onBack: () -> Unit,
    viewModel: WidgetCreatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                WidgetCreatorEffect.NavigateToDashboard -> onBack()
                is WidgetCreatorEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.step) {
                CreatorStep.NL_INPUT -> {
                    NLInputScreen(
                        state = state,
                        onIntent = viewModel::processIntent,
                        onBack = onBack
                    )
                }
                CreatorStep.PROCESSING -> {
                    ProcessingScreen(state = state)
                }
                CreatorStep.TEMPLATE_SELECTION -> {
                    TemplateSelectionScreen(
                        state = state,
                        onIntent = viewModel::processIntent,
                        onBack = { viewModel.processIntent(WidgetCreatorIntent.Back) }
                    )
                }
                CreatorStep.PREVIEW -> {
                    WidgetPreviewScreen(
                        state = state,
                        onIntent = viewModel::processIntent,
                        onBack = { viewModel.processIntent(WidgetCreatorIntent.Back) }
                    )
                }
                CreatorStep.DONE -> {
                    SuccessScreen(
                        state = state,
                        onDismiss = { viewModel.processIntent(WidgetCreatorIntent.Dismiss) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessingScreen(state: WidgetCreatorState) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = state.processingMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This may take a few seconds on-device...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SuccessScreen(
    state: WidgetCreatorState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pinRequested by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Widget Created!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.createdWidget?.name ?: "",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (pinRequested) "Approve the launcher prompt to add it to your home screen."
            else "Add this widget directly to your home screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {
                state.createdWidget?.let { pinRequested = WidgetPinning.request(context, it) }
            },
            enabled = !pinRequested && state.createdWidget != null,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (pinRequested) "Pin request sent" else "Add to Home Screen")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Go to Dashboard")
        }
    }
}
