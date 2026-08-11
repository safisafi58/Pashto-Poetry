package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {
    @Query("SELECT * FROM poems ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllPoems(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE isApproved = 1 ORDER BY isFeatured DESC, createdAt DESC")
    fun getApprovedPoems(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE isApproved = 0 ORDER BY createdAt DESC")
    fun getPendingPoems(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE category = :category AND isApproved = 1 ORDER BY createdAt DESC")
    fun getPoemsByCategory(category: String): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE poetId = :poetId AND isApproved = 1 ORDER BY createdAt DESC")
    fun getPoemsByPoet(poetId: String): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE authorUserId = :userId ORDER BY createdAt DESC")
    fun getPoemsByAuthorUser(userId: String): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE id = :id LIMIT 1")
    suspend fun getPoemById(id: String): PoemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoem(poem: PoemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoems(poems: List<PoemEntity>)

    @Update
    suspend fun updatePoem(poem: PoemEntity)

    @Query("DELETE FROM poems WHERE id = :id")
    suspend fun deletePoemById(id: String)

    @Query("SELECT COUNT(*) FROM poems")
    suspend fun getPoemCount(): Int

    @Query("UPDATE poems SET likesCount = likesCount + :delta WHERE id = :poemId")
    suspend fun updateLikesCount(poemId: String, delta: Int)

    @Query("UPDATE poems SET favoritesCount = favoritesCount + :delta WHERE id = :poemId")
    suspend fun updateFavoritesCount(poemId: String, delta: Int)

    @Query("UPDATE poems SET commentsCount = commentsCount + :delta WHERE id = :poemId")
    suspend fun updateCommentsCount(poemId: String, delta: Int)
}

@Dao
interface PoetDao {
    @Query("SELECT * FROM poets ORDER BY name ASC")
    fun getAllPoets(): Flow<List<PoetEntity>>

    @Query("SELECT * FROM poets WHERE id = :id LIMIT 1")
    suspend fun getPoetById(id: String): PoetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoet(poet: PoetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoets(poets: List<PoetEntity>)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE poemId = :poemId ORDER BY createdAt DESC")
    fun getCommentsForPoem(poemId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY createdAt DESC")
    fun getAllComments(): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)
}

@Dao
interface LikeDao {
    @Query("SELECT * FROM likes WHERE userId = :userId")
    fun getLikesForUser(userId: String): Flow<List<LikeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE poemId = :poemId AND userId = :userId)")
    fun isLikedByUser(poemId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("DELETE FROM likes WHERE poemId = :poemId AND userId = :userId")
    suspend fun deleteLike(poemId: String, userId: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE poemId = :poemId AND userId = :userId)")
    fun isFavoriteByUser(poemId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE poemId = :poemId AND userId = :userId")
    suspend fun deleteFavorite(poemId: String, userId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isAdmin = 1 ORDER BY createdAt DESC")
    fun getAdminUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET isAdmin = :isAdmin WHERE id = :userId")
    suspend fun updateUserAdminRole(userId: String, isAdmin: Boolean)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
}
