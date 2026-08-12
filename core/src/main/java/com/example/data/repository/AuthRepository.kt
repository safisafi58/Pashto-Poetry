package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserEntity
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()

    val currentUser: Flow<UserProfile> = userDao.getCurrentUser().map { entity ->
        if (entity != null) {
            UserProfile(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                avatarUrl = entity.avatarUrl,
                bio = entity.bio,
                isAdmin = entity.isAdmin,
                isVerifiedPoet = entity.isVerifiedPoet,
                isLoggedIn = entity.isLoggedIn,
                createdAt = entity.createdAt,
                telegramBotToken = entity.telegramBotToken,
                telegramChannelId = entity.telegramChannelId
            )
        } else {
            UserProfile() // Default guest profile
        }
    }

    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsers().map { list ->
        list.map {
            UserProfile(
                id = it.id,
                name = it.name,
                email = it.email,
                avatarUrl = it.avatarUrl,
                bio = it.bio,
                isAdmin = it.isAdmin,
                isVerifiedPoet = it.isVerifiedPoet,
                isLoggedIn = it.isLoggedIn,
                createdAt = it.createdAt,
                telegramBotToken = it.telegramBotToken,
                telegramChannelId = it.telegramChannelId
            )
        }
    }

    suspend fun login(email: String, name: String, isAdmin: Boolean = false): UserProfile {
        userDao.logoutAllUsers()
        val userId = if (isAdmin) "admin_user_01" else "user_" + email.hashCode()
        val user = UserEntity(
            id = userId,
            name = if (name.isNotBlank()) name else (if (isAdmin) "اداري اډمین" else "پښتون شاعردوست"),
            email = email,
            avatarUrl = null,
            bio = if (isAdmin) "د پښتو شعرونو سستم سمبالونکی" else "د پښتو خوږ ادب او شعرونو مینه وال",
            isAdmin = isAdmin,
            isVerifiedPoet = !isAdmin,
            isLoggedIn = true,
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
        return UserProfile(user.id, user.name, user.email, user.avatarUrl, user.bio, user.isAdmin, user.isVerifiedPoet, true, user.createdAt)
    }

    suspend fun updateProfile(name: String, bio: String, avatarUrl: String?) {
        val current = userDao.getUserById("guest_user") ?: return
        val updated = current.copy(name = name, bio = bio, avatarUrl = avatarUrl)
        userDao.insertUser(updated)
    }

    suspend fun logout() {
        userDao.logoutAllUsers()
    }
}
