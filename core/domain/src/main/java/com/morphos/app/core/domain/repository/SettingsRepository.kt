package com.morphos.app.core.domain.repository

import com.morphos.app.core.common.AppResult
import com.morphos.app.core.domain.model.NotificationPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface SettingsRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(prefs: UserPreferences): AppResult<Unit>
    suspend fun isOnboardingComplete(): Boolean
    suspend fun setOnboardingComplete(): AppResult<Unit>
    suspend fun isCloudAiEnabled(): Boolean
    suspend fun setCloudAiEnabled(enabled: Boolean): AppResult<Unit>
    suspend fun getRetentionDays(): Int
    suspend fun setRetentionDays(days: Int): AppResult<Unit>
}

@Serializable
data class UserPreferences(
    val isOnboardingComplete: Boolean = false,
    val isCloudAiEnabled: Boolean = false,
    val retentionDays: Int = 30,
    val defaultWidgetTheme: String = "system",
    val notificationPreference: NotificationPreference = NotificationPreference.MODERATE,
    val enableHaptics: Boolean = true,
    val enableAnimations: Boolean = true,
    val lastSeen: Long = 0L
)
