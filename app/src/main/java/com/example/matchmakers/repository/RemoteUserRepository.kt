package com.example.matchmakers.repository

import android.util.Log
import com.example.matchmakers.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class RemoteUserRepository {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDb.collection("users")
    private val profileInfoCollection = firebaseDb.collection("profileinfo")
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "RemoteUserRepository"

    fun getCurrentUserId(): String? {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Logged-in User ID: $userId")
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
     * Excludes users in the logged-in user's `DislikeList` and `MatchedMe`.
     */
    suspend fun getRecommendedUsers(recommendedClusters: List<Int>): List<User> {
        Log.d(TAG, "getRecommendedUsers: Fetching users for clusters = $recommendedClusters")
        val currentUserId = getCurrentUserId()

        if (currentUserId == null) {
            Log.e(TAG, "getRecommendedUsers: Current user ID is null. Cannot fetch recommendations.")
            return emptyList()
        }

        return try {
            // Fetch the logged-in user's DislikeList and MatchedMe
//            val currentUserDoc = userCollection.document(currentUserId).get().await()
//            val dislikeList = currentUserDoc.get("DislikeList") as? List<String> ?: emptyList()
//            val matchedMeList = currentUserDoc.get("MatchedMe") as? List<String> ?: emptyList()

            // Fetch the logged-in user's DislikeList and MatchedMe from profileinfo
            val profileInfoDoc = profileInfoCollection.document(currentUserId).get().await()
            val dislikeList = profileInfoDoc.get("DislikeList") as? List<String> ?: emptyList()
            val matchedMeList = profileInfoDoc.get("MatchedMe") as? List<String> ?: emptyList()
            val likeList = profileInfoDoc.get("LikeList") as? List<String> ?: emptyList()

            Log.d(TAG, "getRecommendedUsers: Excluding users in DislikeList: $dislikeList")
//            Log.d(TAG, "getRecommendedUsers: Excluding users in MatchedMe: $matchedMeList")
            Log.d(TAG, "getRecommendedUsers: Excluding users in likeList: $likeList")

            // Fetch recommended users based on clusters
            val querySnapshot = userCollection.whereIn("cluster", recommendedClusters)
                .limit(60)
                .get()
                .await()

            val users = querySnapshot.documents.map { document ->
                val user = document.toObject(User::class.java)
                user?.copy(id = document.id, lastUpdated = System.currentTimeMillis()) // Assign the document ID to the `id` field
            }.filterNotNull() // Filter out any null users

            // Exclude users in DislikeList and likeList
            val filteredUsers = users.filter { user ->
                user.id !in dislikeList && user.id !in likeList
            }

            Log.d(TAG, "getRecommendedUsers: Retrieved ${filteredUsers.size} users after filtering.")
            filteredUsers
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
    /**
     * Handles the Dislike action.
     */
    suspend fun handleDislikeAction(currentUserId: String, dislikedUserId: String) {
        try {
            Log.d(TAG, "Disliking User: $dislikedUserId for User: $currentUserId")

            // Update DislikeList of the logged-in user in the profileinfo collection
            profileInfoCollection.document(currentUserId)
                .update("DislikeList", FieldValue.arrayUnion(dislikedUserId))
                .await()
            Log.d(TAG, "Added $dislikedUserId to $currentUserId's DislikeList in profileinfo.")

            // Update DislikedMe of the disliked user in the profileinfo collection
            profileInfoCollection.document(dislikedUserId)
                .update("DislikedMe", FieldValue.arrayUnion(currentUserId))
                .await()
            Log.d(TAG, "Added $currentUserId to $dislikedUserId's DislikedMe in profileinfo.")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling dislike action: ${e.message}", e)
        }
    }

    /**
     * Handles the Like action using the profileinfo collection.
     */
    suspend fun handleLikeAction(currentUserId: String, likedUserId: String) {
        try {
            Log.d(TAG, "Liking User: $likedUserId for User: $currentUserId")

            // Update LikeList of the logged-in user in the profileinfo collection
            profileInfoCollection.document(currentUserId)
                .update("LikeList", FieldValue.arrayUnion(likedUserId))
                .await()
            Log.d(TAG, "Added $likedUserId to $currentUserId's LikeList in profileinfo.")

            // Update LikedMe of the liked user in the profileinfo collection
            profileInfoCollection.document(likedUserId)
                .update("LikedMe", FieldValue.arrayUnion(currentUserId))
                .await()
            Log.d(TAG, "Added $currentUserId to $likedUserId's LikedMe in profileinfo.")

            // Check for mutual like
            val likedUserDoc = profileInfoCollection.document(likedUserId).get().await()
            val likedUserLikeList = likedUserDoc.get("LikeList") as? List<String> ?: emptyList()

            if (currentUserId in likedUserLikeList) {
                Log.d(TAG, "Mutual like found! Creating match between $currentUserId and $likedUserId.")

                // Update MatchedMe for both users in the profileinfo collection
                profileInfoCollection.document(currentUserId)
                    .update("MatchedMe", FieldValue.arrayUnion(likedUserId))
                    .await()
                profileInfoCollection.document(likedUserId)
                    .update("MatchedMe", FieldValue.arrayUnion(currentUserId))
                    .await()

                Log.d(TAG, "Match created between $currentUserId and $likedUserId in profileinfo.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling like action: ${e.message}", e)
        }
    }

    /**
     * Background task to clean up `LikedMe` for matched users.
     * This should run periodically or after updating the cache.
     */
    suspend fun cleanUpLikedMe() {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Log.e(TAG, "cleanUpLikedMe: Current user ID is null. Cannot clean up LikedMe.")
            return
        }

        try {
            // Fetch the current user's LikedMe list
            val currentUserDoc = userCollection.document(currentUserId).get().await()
            val likedMeList = currentUserDoc.get("LikedMe") as? List<String> ?: emptyList()
            val matchedMeList = currentUserDoc.get("MatchedMe") as? List<String> ?: emptyList()

            Log.d(TAG, "cleanUpLikedMe: LikedMe = $likedMeList")
            Log.d(TAG, "cleanUpLikedMe: MatchedMe = $matchedMeList")

            // Filter out users who are already in MatchedMe
            val cleanedLikedMe = likedMeList.filterNot { it in matchedMeList }

            // Update the LikedMe list in the database
            userCollection.document(currentUserId).update("LikedMe", cleanedLikedMe).await()
            Log.d(TAG, "cleanUpLikedMe: Updated LikedMe list: $cleanedLikedMe")
        } catch (e: Exception) {
            Log.e(TAG, "cleanUpLikedMe: Error cleaning up LikedMe: ${e.message}", e)
        }
    }

}
