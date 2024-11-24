package com.example.actiontabskotlin.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.matchmakers.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // 1. Get all recommended users
    @Query("SELECT * FROM user_table")
    fun getAllRecommendedUsers(): Flow<List<User>>

    // 2. Insert multiple recommended users (bulk caching)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendedUsers(users: List<User>)

    // 3. Insert a single recommended user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendedUser(user: User)

    // 4. Delete a specific user by ID
    @Query("DELETE FROM user_table WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    // 5. Delete all recommended users
    @Query("DELETE FROM user_table")
    suspend fun deleteAllRecommendedUsers()

    // 6. Get the most recent `lastUpdated` timestamp
    @Query("SELECT MAX(lastUpdated) FROM user_table")
    suspend fun getLastUpdatedTimestamp(): Long?

    // 7. Get the most recent `lastFetched` timestamp
    @Query("SELECT MAX(lastFetched) FROM user_table")
    suspend fun getLastFetchedTimestamp(): Long?

    // 8. Update `lastFetched` timestamp for all users after a refresh
    @Query("UPDATE user_table SET lastFetched = :timestamp")
    suspend fun updateLastFetchedTimestampForAll(timestamp: Long)
}
