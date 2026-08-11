package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poems")
data class PoemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val poetId: String,
    val poetName: String,
    val category: String,
    val authorUserId: String?,
    val isApproved: Boolean,
    val isFeatured: Boolean,
    val likesCount: Int,
    val favoritesCount: Int,
    val commentsCount: Int,
    val createdAt: Long
)

@Entity(tableName = "poets")
data class PoetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val era: String,
    val bio: String,
    val imageUrl: String?,
    val poemCount: Int
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val poemId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val commentText: String,
    val createdAt: Long
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val id: String, // poemId_userId
    val poemId: String,
    val userId: String,
    val createdAt: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // poemId_userId
    val poemId: String,
    val userId: String,
    val createdAt: Long
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val bio: String,
    val isAdmin: Boolean,
    val isVerifiedPoet: Boolean,
    val isLoggedIn: Boolean,
    val createdAt: Long,
    val telegramBotToken: String? = null,
    val telegramChannelId: String? = null
)
