package com.example.matchmakers.repository

import android.util.Log
import com.example.actiontabskotlin.database.UserDao
import com.example.matchmakers.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocalUserRepository(private val userDao: UserDao) {

    private val TAG = "LocalUserRepository"

    /**
     * Logs the current cache size.
     */
    /**
     * Fetches and logs the current cache size, returning the size.
     */
    suspend fun getCacheSize(): Int {
        val size = userDao.getAllRecommendedUsers().first().size
        Log.d(TAG, "Cache size: $size")
        return size
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
     * Forces a cache refresh by reloading all recommended users.
     */
    suspend fun refreshCache() {
        Log.d(TAG, "refreshCache: Refreshing cache from local database.")
        val users = userDao.getAllRecommendedUsers().first()
        Log.d(TAG, "refreshCache: Cache refreshed. Current cache size = ${users.size}")
    }

    /**
     * Inserts a list of recommended users into the cache.
     */
    suspend fun insertRecommendedUsers(users: List<User>) {
        Log.d(TAG, "insertRecommendedUsers: Inserting ${users.size} users into local cache.")
        userDao.insertRecommendedUsers(users)
//        refreshCache() // Refresh cache after insertion
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

    /**
     * Atomically updates the local cache with fetched data.
     * Clears existing users, inserts new users, and updates timestamps in a single transaction.
     */
//    suspend fun updateCache(users: List<User>, timestamp: Long) {
//        Log.d(TAG, "updateCache: Atomically updating cache with ${users.size} users.")
//        deleteAllRecommendedUsers() // Clear old cache
//        insertRecommendedUsers(users) // Insert new data
//        updateLastFetchedTimestampForAll(timestamp) // Update timestamp
//        Log.d(TAG, "updateCache: Cache updated successfully.")
//    }
    suspend fun updateCache(users: List<User>, timestamp: Long, onUpdateComplete: () -> Unit) {
        Log.d(TAG, "updateCache: Updating local cache with ${users.size} users.")
        userDao.deleteAllRecommendedUsers() // Clear the old cache
        userDao.insertRecommendedUsers(users) // Insert new data
        userDao.updateLastFetchedTimestampForAll(timestamp) // Update timestamp
        Log.d(TAG, "updateCache: Cache update completed.")

        // Notify that cache update is complete
        onUpdateComplete()
    }

    /**
     * Runs a block of code within a database transaction.
     */
    suspend fun runInTransaction(transactionBlock: suspend () -> Unit) {
        try {
            Log.d(TAG, "runInTransaction: Starting a database transaction.")
            transactionBlock()
            Log.d(TAG, "runInTransaction: Transaction completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "runInTransaction: Transaction failed with error: ${e.message}", e)
            throw e // Propagate the error
        }
    }

    suspend fun updateCacheWithFetchedData(users: List<User>, timestamp: Long) {
        Log.d(TAG, "updateCacheWithFetchedData: Updating local cache with ${users.size} users.")
        userDao.updateCache(users, timestamp) // Use atomic transaction from UserDao
        Log.d(TAG, "updateCacheWithFetchedData: Cache updated successfully.")
    }



}
