package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TelegramPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("telegram_config", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val botTokenFlow = MutableStateFlow(getSavedBotToken())
    val channelIdFlow = MutableStateFlow(getSavedChannelId())

    fun saveConfig(token: String, channelId: String) {
        prefs.edit()
            .putString("bot_token", token)
            .putString("channel_id", channelId)
            .apply()
        botTokenFlow.value = token
        channelIdFlow.value = channelId
    }

    fun getSavedBotToken(): String {
        return prefs.getString("bot_token", "") ?: ""
    }

    fun getSavedChannelId(): String {
        return prefs.getString("channel_id", "@pashto_poetry") ?: "@pashto_poetry"
    }

    suspend fun fetchChannelPosts(token: String? = null, channelId: String? = null): List<TelegramPost> = withContext(Dispatchers.IO) {
        val activeToken = token?.ifBlank { null } ?: getSavedBotToken().ifBlank { null }
        val rawChannel = channelId?.ifBlank { null } ?: getSavedChannelId().ifBlank { "@pashto_poetry" }
        val cleanChannel = rawChannel.trim().removePrefix("@")

        val posts = mutableListOf<TelegramPost>()

        if (!activeToken.isNullOrBlank()) {
            try {
                val url = "https://api.telegram.org/bot$activeToken/getUpdates?limit=30"
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            if (json.optBoolean("ok")) {
                                val result = json.optJSONArray("result")
                                if (result != null && result.length() > 0) {
                                    for (i in 0 until result.length()) {
                                        val update = result.optJSONObject(i)
                                        val channelPost = update?.optJSONObject("channel_post")
                                            ?: update?.optJSONObject("message")
                                        if (channelPost != null) {
                                            val text = channelPost.optString("text", "")
                                            val date = channelPost.optLong("date", System.currentTimeMillis() / 1000)
                                            val msgId = channelPost.optInt("message_id", i)
                                            val chat = channelPost.optJSONObject("chat")
                                            val chatTitle = chat?.optString("title") ?: "@$cleanChannel"
                                            val username = chat?.optString("username") ?: cleanChannel

                                            if (text.isNotBlank()) {
                                                posts.add(
                                                    TelegramPost(
                                                        id = "tg_$msgId",
                                                        text = text,
                                                        date = formatTelegramDate(date),
                                                        channelName = chatTitle,
                                                        telegramUrl = "https://t.me/$username/$msgId"
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (posts.isEmpty() && cleanChannel.isNotBlank()) {
            try {
                val webUrl = "https://t.me/s/$cleanChannel"
                val request = Request.Builder()
                    .url(webUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; PashtoPoetry)")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val html = response.body?.string() ?: ""
                        val regex = Regex("""<div class="tgme_widget_message_text[^">]*">(.*?)</div>""", RegexOption.DOT_MATCHES_ALL)
                        val matches = regex.findAll(html).toList()

                        for ((idx, match) in matches.withIndex()) {
                            val rawText = match.groupValues[1]
                                .replace("<br/>", "\n")
                                .replace("<br>", "\n")
                                .replace(Regex("<[^>]*>"), "")
                                .trim()

                            if (rawText.isNotBlank()) {
                                posts.add(
                                    TelegramPost(
                                        id = "tg_web_$idx",
                                        text = rawText,
                                        date = "تازه خپره شوې غزل",
                                        channelName = "@$cleanChannel",
                                        telegramUrl = "https://t.me/s/$cleanChannel"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (posts.isEmpty()) {
            posts.addAll(getSamplePashtoTelegramPosts(cleanChannel))
        }

        posts.reversed()
    }

    private fun formatTelegramDate(timestamp: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp * 1000))
        } catch (e: Exception) {
            "تازه خپور شوی"
        }
    }

    private fun getSamplePashtoTelegramPosts(channelName: String): List<TelegramPost> {
        val displayChannel = if (channelName.isNotBlank()) "@$channelName" else "@pashto_poetry"
        return listOf(
            TelegramPost(
                id = "sample_tg_1",
                text = "ما لیدلي دي بې شمېره انسانان\nخو انسان کې مې انسانيت ونلید\n\n— د تلګرام رسمي کانال تازه خپرونه",
                date = "نن 10:30",
                channelName = displayChannel,
                views = "1.8k",
                telegramUrl = "https://t.me/s/${channelName.ifBlank { "pashto_poetry" }}"
            ),
            TelegramPost(
                id = "sample_tg_2",
                text = "د سهار باد دې خبر راوړي له یار\nچې په کوم حالت کې اوسي نوبهار\n\nرحمان بابا - د پښتو خوږ کلام",
                date = "پرون 18:45",
                channelName = displayChannel,
                views = "2.4k",
                telegramUrl = "https://t.me/s/${channelName.ifBlank { "pashto_poetry" }}"
            ),
            TelegramPost(
                id = "sample_tg_3",
                text = "چې د ننګ او غیرت خبره راشي\nخوشحال خان مې په تصور کې درېږي\n\n— حماسي افغاني شعرونه",
                date = "پرون 14:15",
                channelName = displayChannel,
                views = "3.2k",
                telegramUrl = "https://t.me/s/${channelName.ifBlank { "pashto_poetry" }}"
            )
        )
    }
}
