package com.morphos.app.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                PreferenceHeader(title = "App Permissions Integration")
            }

            item {
                RuntimePermissionRow(
                    permission = Manifest.permission.READ_CALENDAR,
                    icon = Icons.Default.CalendarToday,
                    title = "Calendar",
                    description = "Required to display meetings and calendar events inside widgets."
                )
            }

            item {
                RuntimePermissionRow(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    icon = Icons.Default.LocationOn,
                    title = "Location Access",
                    description = "Allows context agent to suggest weather and location specific bindings."
                )
            }

            item {
                RuntimePermissionRow(
                    permission = Manifest.permission.ACTIVITY_RECOGNITION,
                    icon = Icons.Default.DirectionsRun,
                    title = "Activity Recognition",
                    description = "Required to fetch step counts and daily walk goals."
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    RuntimePermissionRow(
                        permission = Manifest.permission.POST_NOTIFICATIONS,
                        icon = Icons.Default.Notifications,
                        title = "Notifications access",
                        description = "Required to prioritize incoming alerts and summarize notifications."
                    )
                }
            }

            item {
                UsageStatsPermissionRow()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RuntimePermissionRow(
    permission: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    val permissionState = rememberPermissionState(permission)
    val isGranted = permissionState.status.isGranted

    PermissionItemView(
        icon = icon,
        title = title,
        description = description,
        isGranted = isGranted,
        onGrantClick = { permissionState.launchPermissionRequest() }
    )
}

@Composable
fun UsageStatsPermissionRow() {
    val context = LocalContext.current
    val isGranted = checkUsageStatsPermission(context)

    PermissionItemView(
        icon = Icons.Default.SettingsSuggest,
        title = "Usage Telemetry stats",
        description = "Provides application usage times to dynamically classify context.",
        isGranted = isGranted,
        onGrantClick = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.fromParts("package", context.packageName, null)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        }
    )
}

@Composable
fun PermissionItemView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isGranted) {
                Text(
                    text = "Active",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}
