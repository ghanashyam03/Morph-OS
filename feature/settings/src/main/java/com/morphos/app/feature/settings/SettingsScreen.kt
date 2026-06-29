package com.morphos.app.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    Text("Settings Screen Stub")
}
