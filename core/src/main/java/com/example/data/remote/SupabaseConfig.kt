package com.example.data.remote

import android.content.Context

object SupabaseConfig {
    var supabaseUrl: String = "https://hzfzxbenlztnsuknsevv.supabase.co"
    var supabaseKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh6Znp4YmVubHp0bnN1a25zZXZ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYzNTk4OTUsImV4cCI6MjEwMTkzNTg5NX0.1the1ojasrIJUyrossUfcWgRO1BDXSdDG48RtSQ2i4g"
    var isConnected: Boolean = true

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("url", null)
        val savedKey = prefs.getString("key", null)
        if (!savedUrl.isNullOrBlank() && !savedKey.isNullOrBlank()) {
            supabaseUrl = savedUrl
            supabaseKey = savedKey
            isConnected = true
        }
    }

    fun saveConfig(context: Context, url: String, key: String) {
        supabaseUrl = url.trim().removeSuffix("/")
        supabaseKey = key.trim()
        isConnected = true
        val prefs = context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("url", supabaseUrl).putString("key", supabaseKey).apply()
    }

    fun isConfigured(): Boolean {
        return supabaseUrl.contains("supabase.co") && !supabaseUrl.contains("your-project") && supabaseKey != "your-anon-key"
    }
}
