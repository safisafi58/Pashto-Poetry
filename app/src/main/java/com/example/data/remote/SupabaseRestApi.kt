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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class SupabaseConnectionResult(
    val isSuccess: Boolean,
    val statusCode: Int? = null,
    val errorMessage: String
)

class SupabaseRestApi {
    private val client: OkHttpClient = createUnsafeOkHttpClient()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    suspend fun testConnectionDetail(url: String, key: String): SupabaseConnectionResult {
        val cleanUrl = url.trim().removeSuffix("/")
        val cleanKey = key.trim()

        if (cleanUrl.isBlank() || cleanKey.isBlank()) {
            return SupabaseConnectionResult(false, null, "لطفاً د Supabase URL او Anon Key دننه کړئ.")
        }
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            return SupabaseConnectionResult(false, null, "د Supabase URL باید له http:// یا https:// سره پیل شي.")
        }

        val endpointsToTry = listOf(
            "$cleanUrl/rest/v1/",
            "$cleanUrl/auth/v1/health",
            "$cleanUrl/rest/v1/poems?select=id&limit=1"
        )

        var lastErrorResult: SupabaseConnectionResult? = null

        for (targetUrl in endpointsToTry) {
            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .addHeader("apikey", cleanKey)
                    .addHeader("Authorization", "Bearer $cleanKey")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val bodyString = try { response.body?.string() ?: "" } catch (e: Exception) { "" }

                    if (response.isSuccessful) {
                        return SupabaseConnectionResult(
                            isSuccess = true,
                            statusCode = code,
                            errorMessage = "له Supabase سره بریالۍ اړیکه ټینګه شوه! (HTTP $code)"
                        )
                    } else {
                        val parsedError = when (code) {
                            401 -> "401 Unauthorized: د Anon Key ناسم دی یا موده یې پای ته رسېدلې (Invalid JWT/Key)."
                            403 -> "403 Forbidden / RLS Blocked: د سوبابېس د جلا جدول د لاسرسي پالیسي (RLS Policy) بلاک شوې ده."
                            404 -> "404 Not Found: د Supabase پروژې نښه یا پاڼه ونه موندل شوه ($targetUrl)."
                            in 500..599 -> "HTTP $code Server Error: د Supabase سرور داخلي ستونزه."
                            else -> "HTTP $code Error: ${extractServerErrorMessage(bodyString).ifBlank { response.message }}"
                        }

                        lastErrorResult = SupabaseConnectionResult(false, code, parsedError)

                        if (code == 401) {
                            return lastErrorResult!!
                        }
                    }
                }
            } catch (e: java.net.UnknownHostException) {
                return SupabaseConnectionResult(false, null, "د شبکې تېروتنه (DNS/Host): د سرور آدرس ناپیژندل شوی دی. انټرنیټ یا د پروژې URL وڅېړئ.")
            } catch (e: java.net.SocketTimeoutException) {
                return SupabaseConnectionResult(false, null, "د مهال پای (Timeout Error): له سرور سره اړیکه ځنډېدلې ده. مهرباني وکړئ بیرته هڅه وکړئ.")
            } catch (e: javax.net.ssl.SSLException) {
                return SupabaseConnectionResult(false, null, "د SSL نښه کولو تېروتنه: ${e.localizedMessage ?: e.message}")
            } catch (e: Exception) {
                lastErrorResult = SupabaseConnectionResult(false, null, "د پیوستون تېروتنه: ${e.localizedMessage ?: e.message}")
            }
        }

        return lastErrorResult ?: SupabaseConnectionResult(false, null, "له سوبابېس سره اړیکه ونه شوه.")
    }

    suspend fun testConnection(url: String, key: String): Boolean {
        return testConnectionDetail(url, key).isSuccess
    }

    private fun extractServerErrorMessage(jsonBody: String): String {
        return try {
            if (jsonBody.isBlank()) return ""
            val json = JSONObject(jsonBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("msg") -> json.getString("msg")
                json.has("error") -> json.getString("error")
                json.has("hint") -> json.getString("hint")
                else -> ""
            }
        } catch (e: Exception) {
            ""
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
            Log.e("SupabaseRestApi", "Fetch poems error: ${e.message}", e)
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
