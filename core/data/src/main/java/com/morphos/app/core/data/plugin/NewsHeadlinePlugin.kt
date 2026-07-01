package com.morphos.app.core.data.plugin

import android.content.Context
import com.morphos.app.core.common.AppResult
import com.morphos.app.core.common.safeCall
import com.morphos.app.core.domain.model.*
import com.morphos.app.core.domain.repository.DataPlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject

class NewsHeadlinePlugin @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) : DataPlugin {

    override val pluginId: String = "news_rss"
    override val displayName: String = "News Headline RSS"
    override val requiredPermissions: List<String> = emptyList()

    override val configSchema: PluginConfigSchema = PluginConfigSchema(
        listOf(
            PluginConfigField("feedUrl", "RSS Feed URL", PluginFieldType.URL, true)
        )
    )

    override suspend fun fetch(config: Map<String, String>): AppResult<PluginData> = safeCall {
        val feedUrl = config["feedUrl"] ?: throw IllegalArgumentException("feedUrl config parameter is required")

        val request = Request.Builder().url(feedUrl).build()
        val response = httpClient.newCall(request).execute()
        val xmlContent = response.body?.string() ?: ""

        val articles = parseRss(xmlContent).take(3)

        val rawJson = buildJsonArray {
            articles.forEach { article ->
                add(buildJsonObject {
                    put("title", article.title)
                    put("url", article.link)
                })
            }
        }.toString()

        PluginData(
            pluginId = pluginId,
            dataSourceId = "${pluginId}_source",
            rawValue = rawJson,
            fetchedAt = System.currentTimeMillis()
        )
    }

    override fun canFetch(context: ContextSnapshot): Boolean = context.isConnected

    private fun parseRss(xml: String): List<Article> {
        val articles = mutableListOf<Article>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inItem = false
            var currentTitle = ""
            var currentLink = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("item", ignoreCase = true)) {
                            inItem = true
                        } else if (inItem) {
                            if (name.equals("title", ignoreCase = true)) {
                                currentTitle = parser.nextText() ?: ""
                            } else if (name.equals("link", ignoreCase = true)) {
                                currentLink = parser.nextText() ?: ""
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("item", ignoreCase = true)) {
                            articles.add(Article(currentTitle.trim(), currentLink.trim()))
                            currentTitle = ""
                            currentLink = ""
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // return partial list
        }
        return articles
    }

    private data class Article(val title: String, val link: String)
}
