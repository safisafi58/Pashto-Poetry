package com.example.data.remote

object SupabaseConfig {
    var supabaseUrl: String = "https://your-project.supabase.co"
    var supabaseKey: String = "your-anon-key"
    var isConnected: Boolean = false

    fun isConfigured(): Boolean {
        return supabaseUrl.contains("supabase.co") && !supabaseUrl.contains("your-project") && supabaseKey != "your-anon-key"
    }
}
