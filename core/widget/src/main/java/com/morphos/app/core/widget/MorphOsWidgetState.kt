package com.morphos.app.core.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class MorphOsWidgetState(
    val widgetId: String,
    val widgetName: String,
    val templateId: String,
    val resolvedSlots: Map<String, String>,  // slotId -> resolved string value
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long = 0L
)

object MorphOsWidgetStateDefinition : GlanceStateDefinition<MorphOsWidgetState> {
    private val stateJsonKey = stringPreferencesKey("state_json")
    private val json = Json { ignoreUnknownKeys = true }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.preferencesDataStoreFile("widget_state_$fileKey")
    }

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<MorphOsWidgetState> {
        val prefsDataStore = PreferenceDataStoreFactory.create(
            produceFile = { getLocation(context, fileKey) }
        )

        return object : DataStore<MorphOsWidgetState> {
            override val data: Flow<MorphOsWidgetState> = prefsDataStore.data.map { prefs ->
                val jsonStr = prefs[stateJsonKey]
                if (jsonStr.isNullOrBlank()) {
                    MorphOsWidgetState(
                        widgetId = fileKey,
                        widgetName = "MorphOS Widget",
                        templateId = "TPL_CARD_SINGLE",
                        resolvedSlots = emptyMap(),
                        isLoading = false,
                        errorMessage = null,
                        lastUpdated = 0L
                    )
                } else {
                    try {
                        json.decodeFromString(jsonStr)
                    } catch (e: Exception) {
                        MorphOsWidgetState(
                            widgetId = fileKey,
                            widgetName = "MorphOS Widget",
                            templateId = "TPL_CARD_SINGLE",
                            resolvedSlots = emptyMap(),
                            isLoading = false,
                            errorMessage = "Parse Error: ${e.message}",
                            lastUpdated = 0L
                        )
                    }
                }
            }

            override suspend fun updateData(transform: suspend (t: MorphOsWidgetState) -> MorphOsWidgetState): MorphOsWidgetState {
                var result: MorphOsWidgetState? = null
                prefsDataStore.edit { prefs ->
                    val jsonStr = prefs[stateJsonKey]
                    val current = if (jsonStr.isNullOrBlank()) {
                        MorphOsWidgetState(
                            widgetId = fileKey,
                            widgetName = "MorphOS Widget",
                            templateId = "TPL_CARD_SINGLE",
                            resolvedSlots = emptyMap(),
                            isLoading = false,
                            errorMessage = null,
                            lastUpdated = 0L
                        )
                    } else {
                        try {
                            json.decodeFromString(jsonStr)
                        } catch (e: Exception) {
                            MorphOsWidgetState(
                                widgetId = fileKey,
                                widgetName = "MorphOS Widget",
                                templateId = "TPL_CARD_SINGLE",
                                resolvedSlots = emptyMap(),
                                isLoading = false,
                                errorMessage = null,
                                lastUpdated = 0L
                            )
                        }
                    }
                    val updated = transform(current)
                    prefs[stateJsonKey] = json.encodeToString(updated)
                    result = updated
                }
                return result ?: throw IllegalStateException("Failed to update widget state")
            }
        }
    }
}
