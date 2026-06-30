package com.morphos.app.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morphos.app.core.domain.model.ContextSnapshot
import com.morphos.app.core.domain.model.PrioritizedNotification
import com.morphos.app.core.domain.model.WidgetConfig
import com.morphos.app.core.widget.WidgetTemplateRegistry

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToWidgetCreator: () -> Unit,
    onNavigateToWidgetEditor: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pinInstructionsWidgetId by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmationWidget by remember { mutableStateOf<WidgetConfig?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DashboardEffect.NavigateToWidgetCreator -> onNavigateToWidgetCreator()
                is DashboardEffect.NavigateToWidgetEditor -> onNavigateToWidgetEditor(effect.widgetId)
                is DashboardEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DashboardEffect.ShowPinInstructions -> {
                    pinInstructionsWidgetId = effect.widgetId
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChangeCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MorphOS",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(DashboardIntent.RefreshAll) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Widgets")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.processIntent(DashboardIntent.NavigateToWidgetCreator) },
                icon = { Icon(Icons.Default.Add, "Create Widget") },
                text = { Text("New Widget") },
                shape = RoundedCornerShape(16.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.showEmptyState -> {
                    DashboardEmptyState(onCreateWidget = {
                        viewModel.processIntent(DashboardIntent.NavigateToWidgetCreator)
                    })
                }
                else -> {
                    DashboardContent(
                        state = state,
                        onIntent = viewModel::processIntent,
                        onDeleteWidgetClick = { showDeleteConfirmationWidget = it }
                    )
                }
            }

            // Bottom sheet for pin instructions
            if (pinInstructionsWidgetId != null) {
                PinInstructionsBottomSheet(
                    onDismiss = { pinInstructionsWidgetId = null }
                )
            }

            // Delete Dialog confirmation
            if (showDeleteConfirmationWidget != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationWidget = null },
                    title = { Text("Delete Widget") },
                    text = { Text("Are you sure you want to delete '${showDeleteConfirmationWidget?.name}'? Pinned instances on your home screen will be removed.") },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                showDeleteConfirmationWidget?.let {
                                    viewModel.processIntent(DashboardIntent.DeleteWidget(it.id))
                                }
                                showDeleteConfirmationWidget = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmationWidget = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit,
    onDeleteWidgetClick: (WidgetConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        modifier = modifier.fillMaxSize()
    ) {
        // Notification feed section at top
        if (state.notifications.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                NotificationSummaryCard(notifications = state.notifications)
            }
        }

        // Context snapshot chip bar
        state.contextSnapshot?.let { ctx ->
            item(span = StaggeredGridItemSpan.FullLine) {
                ContextBar(snapshot = ctx)
            }
        }

        // Widgets
        items(state.widgets) { widget ->
            WidgetCard(
                config = widget,
                onTap = {
                    onIntent(DashboardIntent.RecordWidgetTap(widget.id))
                    onIntent(DashboardIntent.NavigateToWidgetEditor(widget.id))
                },
                onDelete = { onDeleteWidgetClick(widget) },
                onPin = { onIntent(DashboardIntent.PinWidgetToHomeScreen(widget.id)) }
            )
        }
    }
}

@Composable
fun ContextBar(snapshot: ContextSnapshot) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${snapshot.batteryLevel}%", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NetworkWifi, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = snapshot.networkType, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (snapshot.locationLatitude != 0.0) "Located" else "Unknown", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetCard(
    config: WidgetConfig,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = { expandedMenu = true }
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = config.templateId.removePrefix("TPL_").lowercase().replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pin Widget") },
                            onClick = {
                                expandedMenu = false
                                onPin()
                            },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Widget") },
                            onClick = {
                                expandedMenu = false
                                onTap()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body (Material Compose Preview of layout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val template = WidgetTemplateRegistry.getTemplate(config.templateId)
                if (template != null) {
                    template.Preview()
                } else {
                    Text(text = "Preview Unavailable", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onPin) {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = "Pin to Home Screen", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onTap) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NotificationSummaryCard(
    notifications: List<PrioritizedNotification>
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notifications.take(3).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = item.appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = item.content, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardEmptyState(
    onCreateWidget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Widgets Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Generate your first on-device dynamic widget using AI.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateWidget,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Widget")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinInstructionsBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Add to Home Screen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "1. Long-press an empty space on your Home Screen.\n" +
                        "2. Select 'Widgets' or 'Add Widget'.\n" +
                        "3. Search or browse for 'MorphOS'.\n" +
                        "4. Drag the widget and release it to place it.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got It")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
