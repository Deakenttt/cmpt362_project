package com.example.matchmakers.repository

import android.util.Log
import com.example.actiontabskotlin.database.UserDao
import com.example.matchmakers.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class LocalUserRepository(private val userDao: UserDao) {

    private val TAG = "LocalUserRepository"

    /**
     * Logs the current cache size.
     */
    fun logCacheSize() {
        runBlocking {
            val users = userDao.getAllRecommendedUsers().first()
            Log.d(TAG, "logCacheSize: Current cache size = ${users.size}")
        }
    }

    /**
     * Fetches all recommended users from the cache.
     */
    fun getAllRecommendedUsers(): Flow<List<User>> {
        return userDao.getAllRecommendedUsers().map { users ->
            Log.d(TAG, "getAllRecommendedUsers: Current cache size = ${users.size}")
            users
        }
    }

    /**
     * Inserts a list of recommended users into the cache.
     */
    suspend fun insertRecommendedUsers(users: List<User>) {
        Log.d(TAG, "insertRecommendedUsers: Inserting ${users.size} users into local cache.")
        userDao.insertRecommendedUsers(users)
        Log.d(TAG, "insertRecommendedUsers: Successfully inserted ${users.size} users.")
    }

    /**
     * Inserts a single recommended user into the cache.
     */
    suspend fun insertRecommendedUser(user: User) {
        Log.d(TAG, "insertRecommendedUser: Inserting user with ID = ${user.id} into local cache.")
        userDao.insertRecommendedUser(user)
        Log.d(TAG, "insertRecommendedUser: Successfully inserted user with ID = ${user.id}.")
    }

    /**
     * Deletes all recommended users from the cache.
     */
    suspend fun deleteAllRecommendedUsers() {
        Log.d(TAG, "deleteAllRecommendedUsers: Deleting all recommended users from local cache.")
        userDao.deleteAllRecommendedUsers()
        Log.d(TAG, "deleteAllRecommendedUsers: Successfully deleted all recommended users.")
    }

    /**
     * Deletes a specific user by their ID from the cache.
     */
    suspend fun deleteUserById(userId: String) {
        Log.d(TAG, "deleteUserById: Deleting user with ID = $userId from local cache.")
        userDao.deleteUserById(userId)
        Log.d(TAG, "deleteUserById: Successfully deleted user with ID = $userId.")
    }

    /**
     * Retrieves the last fetched timestamp from the cache.
     */
    suspend fun getLastFetchedTimestamp(): Long? {
        Log.d(TAG, "getLastFetchedTimestamp: Fetching the last fetched timestamp from local cache.")
        val timestamp = userDao.getLastFetchedTimestamp()
        Log.d(TAG, "getLastFetchedTimestamp: Last fetched timestamp = $timestamp.")
        return timestamp
    }

    /**
     * Updates the last fetched timestamp for all recommended users in the cache.
     */
    suspend fun updateLastFetchedTimestampForAll(timestamp: Long) {
        Log.d(TAG, "updateLastFetchedTimestampForAll: Updating last fetched timestamp to $timestamp.")
        userDao.updateLastFetchedTimestampForAll(timestamp)
        Log.d(TAG, "updateLastFetchedTimestampForAll: Successfully updated last fetched timestamp.")
    }
}
