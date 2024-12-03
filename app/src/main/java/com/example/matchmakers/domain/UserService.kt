package com.example.matchmakers.domain

import android.util.Log
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.repository.RemoteUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class UserService(
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository
) {

    private val TAG = "UserService"
    private val cacheValidityPeriod = 30 * 60 * 1000L // 30 minutes in milliseconds

    /**
     * Public API to get recommended users.
     * Always fetches from the local cache, ensuring the UI doesn't depend on remote operations.
     */
    suspend fun getRecommendedUsers(): List<User> {
        refreshCacheIfNeeded()
        return localUserRepository.getAllRecommendedUsers().first()
    }

    /**
     * Refresh the cache if the data is outdated.
     */
    private suspend fun refreshCacheIfNeeded() {
        val lastFetched = localUserRepository.getLastFetchedTimestamp() ?: 0L
        val currentTime = System.currentTimeMillis()
        // DK comment for testing without cache data (without cache vaild)
        // if the last fetch date is 30 min after the current time, we do conditional updating the cache
//        if (!isCacheValid(lastFetched, currentTime)) {
//            Log.d(TAG, "Cache is invalid. Fetching fresh data from remote...")
//            fetchAndCacheRecommendedUsers(currentTime)
//        } else {
//            Log.d(TAG, "Cache is valid. No need to fetch from remote.")
//        }
        fetchAndCacheRecommendedUsers(currentTime)
    }

    /**
     * Checks if the cache is valid based on the last fetched timestamp.
     */
    private fun isCacheValid(lastFetched: Long, currentTime: Long): Boolean {
        val isValid = currentTime - lastFetched < cacheValidityPeriod
        Log.d(TAG, "Cache valid: $isValid (Last fetched: $lastFetched, Current time: $currentTime)")
        return isValid
    }

    /**
     * Fetches recommended users from the remote repository and caches them locally.
     */
    private suspend fun fetchAndCacheRecommendedUsers(currentTime: Long) {
        val userId = remoteUserRepository.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "User ID is null. Cannot fetch recommended users.")
            return
        }

        val recommendedClusters = remoteUserRepository.getRecommendedClusters(userId)
        if (recommendedClusters.isEmpty()) {
            Log.d(TAG, "No recommended clusters found for user: $userId")
            return
        }

        Log.d(TAG, "Recommended clusters for user $userId: $recommendedClusters")

        val newUsers = fetchIncrementalUpdates(recommendedClusters)
        if (newUsers.isNotEmpty()) {
            Log.d(TAG, "Fetched ${newUsers.size} new recommended users. Updating local cache...")
            updateLocalCache(newUsers, currentTime)
        } else {
            Log.d(TAG, "No new users found for recommended clusters: $recommendedClusters")
        }
    }

    /**
     * Fetches only the new or updated users since the last cache update.
     */
    private suspend fun fetchIncrementalUpdates(recommendedClusters: List<Int>): List<User> {
        val lastFetched = localUserRepository.getLastFetchedTimestamp() ?: 0L
        //DK commend (missing timestamp for testing)
//        return remoteUserRepository.getIncrementalUpdates(lastFetched, recommendedClusters)
        return remoteUserRepository.getRecommendedUsers(recommendedClusters)
    }

    /**
     * Updates the local cache with the fetched users and updates the last fetched timestamp.
     */
//    private suspend fun updateLocalCache(users: List<User>, currentTime: Long) {
//        // Clear the existing cache and insert the new data
//        localUserRepository.deleteAllRecommendedUsers()
//        localUserRepository.insertRecommendedUsers(users)
//        // Log the current cache size to ensure data consistency
//        logCacheData()
//
//        // Update the last fetched timestamp
//        localUserRepository.updateLastFetchedTimestampForAll(currentTime)
//        Log.d(TAG, "Local cache updated with ${users.size} users.")
//    }
//    private suspend fun updateLocalCache(users: List<User>, currentTime: Long) {
//        if (users.isEmpty()) {
//            Log.d(TAG, "No new users to update in the local cache.")
//            return
//        }
//
//        // Perform atomic cache update
//        Log.d(TAG, "Updating cache atomically: Deleting old users and inserting ${users.size} new users.")
//        localUserRepository.runInTransaction {
//            localUserRepository.updateCache(users, currentTime)
//        }
//
//        Log.d(TAG, "Local cache updated with ${users.size} users.")
//        logCacheData() // Log the current cache state for debugging
//    }

    private suspend fun updateLocalCache(users: List<User>, currentTime: Long) {
        if (users.isEmpty()) {
            Log.d(TAG, "No new users to update in the local cache.")
            return
        }

        val loggedInUserId = remoteUserRepository.getCurrentUserId()
        if (loggedInUserId == null) {
            Log.e(TAG, "Logged-in user ID is null. Cannot filter out the logged-in user.")
            return
        }

        // Filter out the logged-in user
        val filteredUsers = users.filter { it.id != loggedInUserId }
        Log.d(TAG, "Updating cache with ${filteredUsers.size} users after filtering out the logged-in user.")

        if (filteredUsers.isEmpty()) {
            Log.d(TAG, "No users to update after filtering. Cache update skipped.")
            return
        }

        Log.d(TAG, "Updating cache with ${users.size} users.")

        // Call the updated `updateCache` method with the onUpdateComplete callback
        localUserRepository.updateCache(filteredUsers, currentTime, onUpdateComplete = {
            Log.d(TAG, "Cache update callback invoked. Post-update actions can proceed.")
//            logCacheData() // Log the current cache state for debugging
        })
    }


    /**
     * Logs the current size of the local cache for debugging purposes.
     */
    private suspend fun logCacheData() {
        val cachedUsers = localUserRepository.getAllRecommendedUsers().firstOrNull()
        val cacheSize = cachedUsers?.size ?: 0
        Log.d(TAG, "Current cache size: $cacheSize")
        cachedUsers?.forEach { user ->
            Log.d(TAG, "Cached user: ID = ${user.id}, Name = ${user.name}, Cluster = ${user.cluster}")
        }
    }
}
