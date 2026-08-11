package com.example.data.remote

import android.util.Log
import com.example.data.model.Poem
import com.example.data.model.Comment
import com.example.data.model.UserProfile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRestApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(url: String, key: String): Boolean {
        return try {
            val request = Request.Builder()
                .url("$url/rest/v1/poems?select=id&limit=1")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseRestApi", "Connection error: ${e.message}")
            false
        }
    }

    suspend fun fetchRemotePoems(): List<Poem> {
        if (!SupabaseConfig.isConfigured()) return emptyList()
        return try {
            val request = Request.Builder()
                .url("${SupabaseConfig.supabaseUrl}/rest/v1/poems?select=*")
                .addHeader("apikey", SupabaseConfig.supabaseKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.supabaseKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val jsonArray = JSONArray(body)
                val poems = mutableListOf<Poem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    poems.add(
                        Poem(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            content = obj.optString("content"),
                            poetId = obj.optString("poet_id"),
                            poetName = obj.optString("poet_name"),
                            category = obj.optString("category"),
                            authorUserId = obj.optString("author_user_id", null),
                            isApproved = obj.optBoolean("is_approved", true),
                            isFeatured = obj.optBoolean("is_featured", false),
                            likesCount = obj.optInt("likes_count", 0),
                            favoritesCount = obj.optInt("favorites_count", 0),
                            commentsCount = obj.optInt("comments_count", 0),
                            createdAt = obj.optLong("created_at", System.currentTimeMillis())
                        )
                    )
                }
                poems
            }
        } catch (e: Exception) {
            Log.e("SupabaseRestApi", "Fetch poems error: ${e.message}")
            emptyList()
        }
    }

    suspend fun insertPoem(poem: Poem): Boolean {
        if (!SupabaseConfig.isConfigured()) return true
        return try {
            val json = JSONObject().apply {
                put("id", poem.id)
                put("title", poem.title)
                put("content", poem.content)
                put("poet_id", poem.poetId)
                put("poet_name", poem.poetName)
                put("category", poem.category)
                put("author_user_id", poem.authorUserId)
                put("is_approved", poem.isApproved)
                put("is_featured", poem.isFeatured)
                put("likes_count", poem.likesCount)
                put("favorites_count", poem.favoritesCount)
                put("comments_count", poem.commentsCount)
                put("created_at", poem.createdAt)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.supabaseUrl}/rest/v1/poems")
                .addHeader("apikey", SupabaseConfig.supabaseKey)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.supabaseKey}")
                .addHeader("Prefer", "return=minimal")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseRestApi", "Insert poem error: ${e.message}")
            false
        }
    }
}
