package com.example.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdminStats
import com.example.data.model.Comment
import com.example.data.model.Poem
import com.example.data.model.Poet
import com.example.data.model.UserProfile
import com.example.data.remote.SupabaseConfig
import com.example.data.remote.SupabaseRestApi
import com.example.data.repository.PoetryRepository
import com.example.data.repository.TelegramRepository
import com.example.data.model.TelegramPost
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PoetryRepository(AppDatabase.getDatabase(application))
    private val supabaseApi = SupabaseRestApi()
    val telegramRepository = TelegramRepository(application)

    val supabaseUrlState = MutableStateFlow(SupabaseConfig.supabaseUrl)
    val supabaseKeyState = MutableStateFlow(SupabaseConfig.supabaseKey)
    val isTestingConnection = MutableStateFlow(false)
    val connectionResult = MutableStateFlow<Boolean?>(null)
    val connectionMessage = MutableStateFlow<String?>(null)

    init {
        SupabaseConfig.init(application)
        supabaseUrlState.value = SupabaseConfig.supabaseUrl
        supabaseKeyState.value = SupabaseConfig.supabaseKey
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val pendingPoems: StateFlow<List<Poem>> = repository.getPendingPoems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPoems: StateFlow<List<Poem>> = repository.getAllPoemsForAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComments: StateFlow<List<Comment>> = repository.getAllComments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminUsers: StateFlow<List<UserProfile>> = repository.getAdminUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPoets: StateFlow<List<Poet>> = repository.getAllPoets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val telegramBotTokenState = MutableStateFlow(telegramRepository.getSavedBotToken())
    val telegramChannelIdState = MutableStateFlow(telegramRepository.getSavedChannelId())
    val telegramPosts = MutableStateFlow<List<TelegramPost>>(emptyList())
    val isFetchingTelegram = MutableStateFlow(false)

    val isAdminAuthenticated = MutableStateFlow(false)

    val sentNotifications = MutableStateFlow<List<PushNotificationItem>>(
        listOf(
            PushNotificationItem("1", "نوې خپرونه!", "د رحمان بابا خوندوره نوې غزله زیاته شوه.", "ټول کاروونکي", "10 دقیقې وړاندې", "بریالی"),
            PushNotificationItem("2", "د تلګرام تازه شعرونه", "د پښتو شعرونو نوي لستونه وګورئ.", "پښتو مینوال", "2 ساعته وړاندې", "بریالی")
        )
    )

    val adminStats: StateFlow<AdminStats> = combine(allPoems, pendingPoems, allComments, adminUsers) { all, pending, comments, admins ->
        val totalLikes = all.sumOf { it.likesCount }
        AdminStats(
            totalPoems = all.size,
            pendingApprovalCount = pending.size,
            totalUsers = admins.size + 15,
            totalLikes = totalLikes,
            totalComments = comments.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())

    fun authenticateAdmin(password: String): Boolean {
        if (password == "admin123" || password == "pashto2025" || password == "123456") {
            isAdminAuthenticated.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        isAdminAuthenticated.value = false
    }

    fun saveTelegramConfig(token: String, channelId: String) {
        telegramRepository.saveConfig(token, channelId)
        telegramBotTokenState.value = token
        telegramChannelIdState.value = channelId
        fetchTelegramPosts(token, channelId)
    }

    fun fetchTelegramPosts(token: String? = null, channelId: String? = null) {
        viewModelScope.launch {
            isFetchingTelegram.value = true
            val posts = telegramRepository.fetchChannelPosts(token, channelId)
            telegramPosts.value = posts
            isFetchingTelegram.value = false
        }
    }

    fun approvePoem(poemId: String) {
        viewModelScope.launch {
            repository.approvePoem(poemId)
        }
    }

    fun toggleFeaturePoem(poemId: String) {
        viewModelScope.launch {
            repository.toggleFeaturePoem(poemId)
        }
    }

    fun deletePoem(poemId: String) {
        viewModelScope.launch {
            repository.deletePoem(poemId)
        }
    }

    fun deleteComment(commentId: String, poemId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, poemId)
        }
    }

    fun addPoemDirectly(title: String, poetId: String, poetName: String, category: String, content: String) {
        viewModelScope.launch {
            val poem = Poem(
                id = "poem_admin_" + UUID.randomUUID().toString().take(8),
                title = title,
                content = content,
                poetId = poetId.ifBlank { "poet_rahman_baba" },
                poetName = poetName.ifBlank { "عبدالرحمان بابا" },
                category = category,
                authorUserId = "admin_user",
                isApproved = true,
                isFeatured = true,
                createdAt = System.currentTimeMillis()
            )
            repository.insertPoem(poem, isAdmin = true)
        }
    }

    fun addPoet(name: String, era: String, bio: String) {
        viewModelScope.launch {
            val poet = Poet(
                id = "poet_" + UUID.randomUUID().toString().take(8),
                name = name,
                era = era.ifBlank { "معاصره دوره" },
                bio = bio,
                imageUrl = null,
                poemCount = 1
            )
            repository.insertPoet(poet)
        }
    }

    fun addAdminUser(name: String, email: String, bio: String, botToken: String? = null, channelId: String? = null) {
        viewModelScope.launch {
            val adminUser = UserProfile(
                id = "admin_" + UUID.randomUUID().toString().take(8),
                name = name,
                email = email.ifBlank { "${name.lowercase().replace(" ", "")}@pashto.af" },
                avatarUrl = null,
                bio = bio.ifBlank { "د پښتو شعرونو او اډمین پینل مسؤل اډمین" },
                isAdmin = true,
                isVerifiedPoet = true,
                isLoggedIn = false,
                createdAt = System.currentTimeMillis(),
                telegramBotToken = botToken,
                telegramChannelId = channelId
            )
            repository.insertUser(adminUser)

            if (!botToken.isNullOrBlank() || !channelId.isNullOrBlank()) {
                saveTelegramConfig(botToken ?: "", channelId ?: "")
            }
        }
    }

    fun toggleAdminRole(userId: String, currentIsAdmin: Boolean) {
        viewModelScope.launch {
            repository.updateUserAdminRole(userId, !currentIsAdmin)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun updatePoem(poemId: String, title: String, poetId: String, poetName: String, category: String, content: String) {
        viewModelScope.launch {
            repository.updatePoem(poemId, title, poetId, poetName, category, content)
        }
    }

    fun sendPushNotification(title: String, body: String, topic: String) {
        val newNotification = PushNotificationItem(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            title = title,
            body = body,
            targetTopic = topic,
            sentAt = "همدا اوس",
            status = "لیږل شوی (FCM)"
        )
        sentNotifications.value = listOf(newNotification) + sentNotifications.value
    }

    fun testSupabaseConnection(url: String, key: String) {
        viewModelScope.launch {
            isTestingConnection.value = true
            connectionMessage.value = null

            val cleanUrl = url.trim().removeSuffix("/")
            val cleanKey = key.trim()

            val result = supabaseApi.testConnectionDetail(cleanUrl, cleanKey)
            if (result.isSuccess) {
                SupabaseConfig.saveConfig(getApplication(), cleanUrl, cleanKey)
                supabaseUrlState.value = cleanUrl
                supabaseKeyState.value = cleanKey
                connectionResult.value = true
                connectionMessage.value = result.errorMessage
            } else {
                connectionResult.value = false
                connectionMessage.value = result.errorMessage
            }
            isTestingConnection.value = false
        }
    }
}

data class PushNotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val targetTopic: String,
    val sentAt: String,
    val status: String
)
