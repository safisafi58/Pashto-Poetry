package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.Comment
import com.example.data.model.Poem
import com.example.data.model.Poet
import com.example.data.model.UserProfile
import com.example.data.remote.SupabaseRestApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PoetryRepository(
    private val db: AppDatabase,
    private val supabaseApi: SupabaseRestApi = SupabaseRestApi()
) {
    private val poemDao = db.poemDao()
    private val poetDao = db.poetDao()
    private val commentDao = db.commentDao()
    private val likeDao = db.likeDao()
    private val favoriteDao = db.favoriteDao()
    private val userDao = db.userDao()

    suspend fun seedInitialDataIfEmpty() {
        if (poemDao.getPoemCount() == 0) {
            poetDao.insertPoets(InitialSeedData.poets)
            poemDao.insertPoems(InitialSeedData.poems)
            userDao.insertUsers(InitialSeedData.adminUsers)
        }
    }

    fun getApprovedPoems(userId: String): Flow<List<Poem>> {
        return combine(
            poemDao.getApprovedPoems(),
            likeDao.getLikesForUser(userId),
            favoriteDao.getFavoritesForUser(userId)
        ) { poems, likes, favorites ->
            val likedSet = likes.map { it.poemId }.toSet()
            val favSet = favorites.map { it.poemId }.toSet()

            poems.map { entity ->
                entity.toDomain(
                    isLiked = likedSet.contains(entity.id),
                    isFav = favSet.contains(entity.id)
                )
            }
        }
    }

    fun getPendingPoems(): Flow<List<Poem>> {
        return poemDao.getPendingPoems().map { list ->
            list.map { it.toDomain(isLiked = false, isFav = false) }
        }
    }

    fun getAllPoemsForAdmin(): Flow<List<Poem>> {
        return poemDao.getAllPoems().map { list ->
            list.map { it.toDomain(isLiked = false, isFav = false) }
        }
    }

    fun getPoemsByCategory(category: String, userId: String): Flow<List<Poem>> {
        return combine(
            poemDao.getPoemsByCategory(category),
            likeDao.getLikesForUser(userId),
            favoriteDao.getFavoritesForUser(userId)
        ) { poems, likes, favorites ->
            val likedSet = likes.map { it.poemId }.toSet()
            val favSet = favorites.map { it.poemId }.toSet()

            poems.map { entity ->
                entity.toDomain(
                    isLiked = likedSet.contains(entity.id),
                    isFav = favSet.contains(entity.id)
                )
            }
        }
    }

    fun getPoemsByPoet(poetId: String, userId: String): Flow<List<Poem>> {
        return combine(
            poemDao.getPoemsByPoet(poetId),
            likeDao.getLikesForUser(userId),
            favoriteDao.getFavoritesForUser(userId)
        ) { poems, likes, favorites ->
            val likedSet = likes.map { it.poemId }.toSet()
            val favSet = favorites.map { it.poemId }.toSet()

            poems.map { entity ->
                entity.toDomain(
                    isLiked = likedSet.contains(entity.id),
                    isFav = favSet.contains(entity.id)
                )
            }
        }
    }

    fun getUserPoems(userId: String): Flow<List<Poem>> {
        return combine(
            poemDao.getPoemsByAuthorUser(userId),
            likeDao.getLikesForUser(userId),
            favoriteDao.getFavoritesForUser(userId)
        ) { poems, likes, favorites ->
            val likedSet = likes.map { it.poemId }.toSet()
            val favSet = favorites.map { it.poemId }.toSet()

            poems.map { entity ->
                entity.toDomain(
                    isLiked = likedSet.contains(entity.id),
                    isFav = favSet.contains(entity.id)
                )
            }
        }
    }

    fun getFavoritePoems(userId: String): Flow<List<Poem>> {
        return combine(
            poemDao.getApprovedPoems(),
            favoriteDao.getFavoritesForUser(userId),
            likeDao.getLikesForUser(userId)
        ) { poems, favorites, likes ->
            val favSet = favorites.map { it.poemId }.toSet()
            val likedSet = likes.map { it.poemId }.toSet()

            poems.filter { favSet.contains(it.id) }.map { entity ->
                entity.toDomain(isLiked = likedSet.contains(entity.id), isFav = true)
            }
        }
    }

    fun getAllPoets(): Flow<List<Poet>> {
        return poetDao.getAllPoets().map { list ->
            list.map { Poet(it.id, it.name, it.era, it.bio, it.imageUrl, it.poemCount) }
        }
    }

    suspend fun getPoemById(poemId: String, userId: String): Poem? {
        val entity = poemDao.getPoemById(poemId) ?: return null
        val isLiked = false
        val isFav = false
        return entity.toDomain(isLiked, isFav)
    }

    suspend fun insertPoem(poem: Poem, isAdmin: Boolean = false) {
        val entity = PoemEntity(
            id = poem.id,
            title = poem.title,
            content = poem.content,
            poetId = poem.poetId,
            poetName = poem.poetName,
            category = poem.category,
            authorUserId = poem.authorUserId,
            isApproved = isAdmin || poem.isApproved,
            isFeatured = poem.isFeatured,
            likesCount = poem.likesCount,
            favoritesCount = poem.favoritesCount,
            commentsCount = poem.commentsCount,
            createdAt = poem.createdAt
        )
        poemDao.insertPoem(entity)
        supabaseApi.insertPoem(poem)
    }

    suspend fun updatePoem(poem: Poem) {
        val entity = PoemEntity(
            id = poem.id,
            title = poem.title,
            content = poem.content,
            poetId = poem.poetId,
            poetName = poem.poetName,
            category = poem.category,
            authorUserId = poem.authorUserId,
            isApproved = poem.isApproved,
            isFeatured = poem.isFeatured,
            likesCount = poem.likesCount,
            favoritesCount = poem.favoritesCount,
            commentsCount = poem.commentsCount,
            createdAt = poem.createdAt
        )
        poemDao.updatePoem(entity)
    }

    suspend fun approvePoem(poemId: String) {
        val poem = poemDao.getPoemById(poemId) ?: return
        poemDao.updatePoem(poem.copy(isApproved = true))
    }

    suspend fun toggleFeaturePoem(poemId: String) {
        val poem = poemDao.getPoemById(poemId) ?: return
        poemDao.updatePoem(poem.copy(isFeatured = !poem.isFeatured))
    }

    suspend fun deletePoem(poemId: String) {
        poemDao.deletePoemById(poemId)
    }

    suspend fun toggleLike(poemId: String, userId: String, isCurrentlyLiked: Boolean) {
        val likeKey = "${poemId}_$userId"
        if (isCurrentlyLiked) {
            likeDao.deleteLike(poemId, userId)
            poemDao.updateLikesCount(poemId, -1)
        } else {
            likeDao.insertLike(LikeEntity(likeKey, poemId, userId, System.currentTimeMillis()))
            poemDao.updateLikesCount(poemId, 1)
        }
    }

    suspend fun toggleFavorite(poemId: String, userId: String, isCurrentlyFav: Boolean) {
        val favKey = "${poemId}_$userId"
        if (isCurrentlyFav) {
            favoriteDao.deleteFavorite(poemId, userId)
            poemDao.updateFavoritesCount(poemId, -1)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(favKey, poemId, userId, System.currentTimeMillis()))
            poemDao.updateFavoritesCount(poemId, 1)
        }
    }

    fun getCommentsForPoem(poemId: String): Flow<List<Comment>> {
        return commentDao.getCommentsForPoem(poemId).map { list ->
            list.map { Comment(it.id, it.poemId, it.userId, it.userName, it.userAvatarUrl, it.commentText, it.createdAt) }
        }
    }

    fun getAllComments(): Flow<List<Comment>> {
        return commentDao.getAllComments().map { list ->
            list.map { Comment(it.id, it.poemId, it.userId, it.userName, it.userAvatarUrl, it.commentText, it.createdAt) }
        }
    }

    suspend fun addComment(comment: Comment) {
        commentDao.insertComment(
            CommentEntity(
                id = comment.id,
                poemId = comment.poemId,
                userId = comment.userId,
                userName = comment.userName,
                userAvatarUrl = comment.userAvatarUrl,
                commentText = comment.commentText,
                createdAt = comment.createdAt
            )
        )
        poemDao.updateCommentsCount(comment.poemId, 1)
    }

    suspend fun updatePoem(poemId: String, title: String, poetId: String, poetName: String, category: String, content: String) {
        val existing = poemDao.getPoemById(poemId)
        if (existing != null) {
            val updated = existing.copy(
                title = title,
                poetId = poetId,
                poetName = poetName,
                category = category,
                content = content
            )
            poemDao.updatePoem(updated)
        }
    }

    suspend fun insertPoet(poet: Poet) {
        poetDao.insertPoet(
            PoetEntity(
                id = poet.id,
                name = poet.name,
                era = poet.era,
                bio = poet.bio,
                imageUrl = poet.imageUrl,
                poemCount = poet.poemCount
            )
        )
    }

    fun getAdminUsers(): Flow<List<UserProfile>> {
        return userDao.getAdminUsers().map { list ->
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
    }

    fun getAllUsers(): Flow<List<UserProfile>> {
        return userDao.getAllUsers().map { list ->
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
    }

    suspend fun updateUserAdminRole(userId: String, isAdmin: Boolean) {
        userDao.updateUserAdminRole(userId, isAdmin)
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    suspend fun insertUser(user: UserProfile) {
        userDao.insertUser(
            UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                avatarUrl = user.avatarUrl,
                bio = user.bio,
                isAdmin = user.isAdmin,
                isVerifiedPoet = user.isVerifiedPoet,
                isLoggedIn = user.isLoggedIn,
                createdAt = user.createdAt,
                telegramBotToken = user.telegramBotToken,
                telegramChannelId = user.telegramChannelId
            )
        )
    }

    suspend fun deleteComment(commentId: String, poemId: String) {
        commentDao.deleteComment(commentId)
        poemDao.updateCommentsCount(poemId, -1)
    }

    private fun PoemEntity.toDomain(isLiked: Boolean, isFav: Boolean): Poem {
        return Poem(
            id = id,
            title = title,
            content = content,
            poetId = poetId,
            poetName = poetName,
            category = category,
            authorUserId = authorUserId,
            isApproved = isApproved,
            isFeatured = isFeatured,
            likesCount = likesCount,
            favoritesCount = favoritesCount,
            commentsCount = commentsCount,
            createdAt = createdAt,
            isLikedByMe = isLiked,
            isFavoriteByMe = isFav
        )
    }
}
