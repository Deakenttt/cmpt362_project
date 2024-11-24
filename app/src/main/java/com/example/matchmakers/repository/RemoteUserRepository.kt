package com.example.matchmakers.repository

import android.util.Log
import com.example.matchmakers.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RemoteUserRepository {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDb.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "RemoteUserRepository"

    fun getCurrentUserId(): String? {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Current User ID: $userId")
        return userId
    }

    /**
     * Fetch recommended clusters for the given user ID.
     */
    suspend fun getRecommendedClusters(userId: String): List<Int> {
        Log.d(TAG, "getRecommendedClusters: Fetching recommended clusters for userId = $userId")
        return try {
            val userDoc = userCollection.document(userId).get().await()
            val recommendedClusters = userDoc.get("recommended") as? List<Int> ?: emptyList()
            Log.d(TAG, "getRecommendedClusters: Retrieved clusters = $recommendedClusters")
            recommendedClusters
        } catch (e: Exception) {
            Log.e(TAG, "getRecommendedClusters: Error fetching recommended clusters: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch recommended users for the given list of clusters.
     */
    suspend fun getRecommendedUsers(recommendedClusters: List<Int>): List<User> {
        Log.d(TAG, "getRecommendedUsers: Fetching users for clusters = $recommendedClusters")
        return try {
            val users = userCollection.whereIn("cluster", recommendedClusters)
                .limit(60)
                .get()
                .await()
                .toObjects(User::class.java)
                .map { user -> user.copy(lastUpdated = System.currentTimeMillis()) }

            Log.d(TAG, "getRecommendedUsers: Retrieved ${users.size} users")
            users
        } catch (e: Exception) {
            Log.e(TAG, "getRecommendedUsers: Error fetching recommended users: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch incremental updates since the given lastFetched timestamp.
     */
    suspend fun getIncrementalUpdates(lastFetched: Long, recommendedClusters: List<Int>): List<User> {
        Log.d(
            TAG,
            "getIncrementalUpdates: Fetching incremental updates since $lastFetched for clusters = $recommendedClusters"
        )
        return try {
            val users = userCollection
                .whereIn("cluster", recommendedClusters)
                .whereGreaterThan("lastUpdated", lastFetched)
                .limit(60)
                .get()
                .await()
                .toObjects(User::class.java)
                .map { user -> user.copy(lastUpdated = System.currentTimeMillis()) }

            Log.d(TAG, "getIncrementalUpdates: Retrieved ${users.size} updated users")
            users
        } catch (e: Exception) {
            Log.e(TAG, "getIncrementalUpdates: Error fetching incremental updates: ${e.message}", e)
            emptyList()
        }
    }

}
