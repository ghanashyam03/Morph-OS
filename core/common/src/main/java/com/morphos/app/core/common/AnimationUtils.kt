package com.morphos.app.core.common

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalAnimationsEnabled = compositionLocalOf { true }

@Composable
fun animationsEnabled(): Boolean {
    val context = LocalContext.current
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        scale > 0f
    } catch (e: Exception) {
        true
    }
}
