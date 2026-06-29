package com.morphos.app.core.data.repository

import com.morphos.app.core.common.AppDispatchers
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.data.datastore.UserPreferencesDataSource
import com.morphos.app.core.domain.repository.SettingsRepository
import com.morphos.app.core.domain.repository.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val dispatchers: AppDispatchers
) : SettingsRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return userPreferencesDataSource.getUserPreferencesFlow()
    }

    override suspend fun updateUserPreferences(prefs: UserPreferences): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            userPreferencesDataSource.updateUserPreferences(prefs)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun isOnboardingComplete(): Boolean = withContext(dispatchers.io) {
        try {
            userPreferencesDataSource.getUserPreferences().isOnboardingComplete
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun setOnboardingComplete(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            val current = userPreferencesDataSource.getUserPreferences()
            userPreferencesDataSource.updateUserPreferences(current.copy(isOnboardingComplete = true))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun isCloudAiEnabled(): Boolean = withContext(dispatchers.io) {
        try {
            userPreferencesDataSource.getUserPreferences().isCloudAiEnabled
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun setCloudAiEnabled(enabled: Boolean): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            val current = userPreferencesDataSource.getUserPreferences()
            userPreferencesDataSource.updateUserPreferences(current.copy(isCloudAiEnabled = enabled))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun getRetentionDays(): Int = withContext(dispatchers.io) {
        try {
            userPreferencesDataSource.getUserPreferences().retentionDays
        } catch (e: Exception) {
            30
        }
    }

    override suspend fun setRetentionDays(days: Int): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            val current = userPreferencesDataSource.getUserPreferences()
            userPreferencesDataSource.updateUserPreferences(current.copy(retentionDays = days))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
