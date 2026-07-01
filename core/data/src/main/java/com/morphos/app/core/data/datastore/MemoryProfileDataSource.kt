package com.morphos.app.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.morphos.app.core.domain.model.MemoryProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class MemoryProfileDataSource @Inject constructor(
    @Named("memory_profile") private val dataStore: DataStore<Preferences>
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val MEMORY_PROFILE_KEY = stringPreferencesKey("memory_profile_json")

    suspend fun getMemoryProfile(): MemoryProfile {
        val jsonStr = dataStore.data.first()[MEMORY_PROFILE_KEY]
        return if (jsonStr.isNullOrBlank()) {
            MemoryProfile()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                MemoryProfile()
            }
        }
    }

    suspend fun saveMemoryProfile(profile: MemoryProfile) {
        dataStore.edit { preferences ->
            preferences[MEMORY_PROFILE_KEY] = json.encodeToString(profile)
        }
    }

    fun getMemoryProfileFlow(): Flow<MemoryProfile> {
        return dataStore.data.map { preferences ->
            val jsonStr = preferences[MEMORY_PROFILE_KEY]
            if (jsonStr.isNullOrBlank()) {
                MemoryProfile()
            } else {
                try {
                    json.decodeFromString(jsonStr)
                } catch (e: Exception) {
                    MemoryProfile()
                }
            }
        }
    }
}
