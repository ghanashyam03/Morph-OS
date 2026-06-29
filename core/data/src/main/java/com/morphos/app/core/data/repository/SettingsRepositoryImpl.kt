package com.morphos.app.core.data.repository

import com.morphos.app.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {
    override fun getSetting(key: String, defaultValue: String): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun saveSetting(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        TODO("Not yet implemented")
    }
}
