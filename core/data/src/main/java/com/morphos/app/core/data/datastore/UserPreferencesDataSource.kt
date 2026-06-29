package com.morphos.app.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.morphos.app.core.domain.repository.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

class UserPreferencesDataSource @Inject constructor(
    @Named("user_preferences") private val dataStore: DataStore<Preferences>
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val USER_PREFS_KEY = stringPreferencesKey("user_preferences_json")

    suspend fun getUserPreferences(): UserPreferences {
        val jsonStr = dataStore.data.first()[USER_PREFS_KEY]
        return if (jsonStr.isNullOrBlank()) {
            UserPreferences()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                UserPreferences()
            }
        }
    }

    suspend fun updateUserPreferences(prefs: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[USER_PREFS_KEY] = json.encodeToString(prefs)
        }
    }

    fun getUserPreferencesFlow(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            val jsonStr = preferences[USER_PREFS_KEY]
            if (jsonStr.isNullOrBlank()) {
                UserPreferences()
            } else {
                try {
                    json.decodeFromString(jsonStr)
                } catch (e: Exception) {
                    UserPreferences()
                }
            }
        }
    }
}
