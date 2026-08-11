package com.example.data.remote

object SupabaseConfig {
    var supabaseUrl: String = "https://hzfzxbenlztnsuknsevv.supabase.co"
    var supabaseKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh6Znp4YmVubHp0bnN1a25zZXZ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYzNTk4OTUsImV4cCI6MjEwMTkzNTg5NX0.1the1ojasrIJUyrossUfcWgRO1BDXSdDG48RtSQ2i4g"
    var isConnected: Boolean = true

    fun isConfigured(): Boolean {
        return supabaseUrl.contains("supabase.co") && !supabaseUrl.contains("your-project") && supabaseKey != "your-anon-key"
    }
}
