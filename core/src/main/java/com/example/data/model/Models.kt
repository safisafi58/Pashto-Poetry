package com.example.data.model

import java.util.UUID

enum class PoemCategory(val id: String, val pashtoName: String, val englishName: String) {
    ALL("all", "ټول", "All"),
    GHAZAL("ghazal", "غزلې", "Ghazals"),
    LANDAY("landay", "لنډۍ", "Landay"),
    NAZM("nazm", "نظمونه", "Nazm"),
    QUATRAIN("quatrain", "څلوريزې", "Quatrains"),
    PATRIOTIC("patriotic", "ملي شعرونه", "Patriotic"),
    ROMANCE("romance", "مينې شعرونه", "Romance"),
    ISLAMIC("islamic", "اسلامي شعرونه", "Islamic")
}

data class Poem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val poetId: String,
    val poetName: String,
    val category: String,
    val authorUserId: String? = null,
    val isApproved: Boolean = true,
    val isFeatured: Boolean = false,
    val likesCount: Int = 0,
    val favoritesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isFavoriteByMe: Boolean = false
)

data class Poet(
    val id: String,
    val name: String,
    val era: String,
    val bio: String,
    val imageUrl: String? = null,
    val poemCount: Int = 0
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val poemId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val commentText: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String = "guest_user",
    val name: String = "مېلمه کارونکی",
    val email: String = "guest@pashto.org",
    val avatarUrl: String? = null,
    val bio: String = "د پښتو شعرونو او ادب مینه وال",
    val isAdmin: Boolean = false,
    val isVerifiedPoet: Boolean = false,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val telegramBotToken: String? = null,
    val telegramChannelId: String? = null
)

data class TelegramPost(
    val id: String,
    val text: String,
    val date: String,
    val channelName: String,
    val views: String = "0",
    val telegramUrl: String? = null
)

data class AdminStats(
    val totalPoems: Int = 0,
    val pendingApprovalCount: Int = 0,
    val totalUsers: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0
)
