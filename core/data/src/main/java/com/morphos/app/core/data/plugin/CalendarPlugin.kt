package com.morphos.app.core.data.plugin

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class CalendarPlugin @Inject constructor(
    @ApplicationContext private val context: Context
) : DataPlugin {

    override val pluginId: String = "calendar"
    override val displayName: String = "Calendar Events"
    override val requiredPermissions: List<String> = listOf(Manifest.permission.READ_CALENDAR)

    override val configSchema: PluginConfigSchema = PluginConfigSchema(emptyList())

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val now = System.currentTimeMillis()
        val oneDayLater = now + 24 * 60 * 60 * 1000L

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val rawJson = if (hasPermission) {
            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.EVENT_LOCATION
            )
            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.DELETED} = 0"
            val selectionArgs = arrayOf(now.toString(), oneDayLater.toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

            var eventJson = "{}"
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                    val startIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                    val locIdx = cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)

                    val title = cursor.getString(titleIdx) ?: ""
                    val startTime = cursor.getLong(startIdx)
                    val location = cursor.getString(locIdx) ?: ""

                    eventJson = buildJsonObject {
                        put("title", title)
                        put("startTime", startTime)
                        put("location", location)
                    }.toString()
                }
            }
            eventJson
        } else {
            "{}"
        }

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = now
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = true
}
