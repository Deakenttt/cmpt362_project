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

    suspend fun getRecommendedClusters(userId: String): List<Int> {
        return try {
            val userDoc = userCollection.document(userId).get().await()
            userDoc.get("recommended") as? List<Int> ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recommended clusters: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecommendedUsers(recommendedClusters: List<Int>): List<User> {
        return try {
            userCollection.whereIn("cluster", recommendedClusters)
                .limit(60)
                .get()
                .await()
                .toObjects(User::class.java)
                .map { user -> user.copy(lastUpdated = System.currentTimeMillis()) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recommended users: ${e.message}")
            emptyList()
        }
    }

    suspend fun getIncrementalUpdates(lastFetched: Long, recommendedClusters: List<Int>): List<User> {
        return try {
            userCollection
                .whereIn("cluster", recommendedClusters)
                .whereGreaterThan("lastUpdated", lastFetched)
                .limit(60)
                .get()
                .await()
                .toObjects(User::class.java)
                .map { user -> user.copy(lastUpdated = System.currentTimeMillis()) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching incremental updates: ${e.message}")
            emptyList()
        }
    }

}
