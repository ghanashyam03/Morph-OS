package com.morphos.app.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSetting(key: String, defaultValue: String): Flow<String>
    suspend fun saveSetting(key: String, value: String)
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
