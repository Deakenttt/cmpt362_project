package com.example.matchmakers.repository

import com.example.actiontabskotlin.database.UserDao
import com.example.matchmakers.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDb.collection("users")
    private val auth = FirebaseAuth.getInstance()

    // Get the current logged-in user's ID
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Fetch recommended users for the current user based on their recommended clusters
    suspend fun getRecommendedUsersForCurrentUser(): List<User> {
        val userId = getCurrentUserId() ?: return emptyList()
        return getRecommendedUsersForUser(userId)
    }

    // Refresh recommended users in Room by fetching from Firebase
    suspend fun refreshRecommendedUsersForUser(userId: String) {
        val recommendedUsers = getRecommendedUsersForUser(userId)
        if (recommendedUsers.isNotEmpty()) {
            userDao.deleteAllRecommendedUsers()
            userDao.insertRecommendedUsers(recommendedUsers)
        }
    }

    // Fetch recommended users based on clusters
    suspend fun getRecommendedUsersForUser(userId: String): List<User> {
        return try {
            val userDoc = userCollection.document(userId).get().await()
            val recommendedClusters = userDoc.get("recommended") as? List<Int> ?: emptyList()

            if (recommendedClusters.isNotEmpty()) {
                userCollection.whereIn("cluster", recommendedClusters)
                    .limit(60)
                    .get()
                    .await()
                    .toObjects(User::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fetch users with a specific interest from Firebase (remote)
    suspend fun getUsersByInterestRemote(interest: String): List<User> {
        return try {
            // Query Firebase to get users who have the specified interest
            userCollection.whereEqualTo("interest", interest)
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()  // Return an empty list if there's an error
        }
    }

    // Fetch from Room (local)
    fun getUsersByInterestLocal(interest: String): Flow<List<User>> = userDao.getUsersByInterest(interest)
    fun getAllUsersLocal(): Flow<List<User>> = userDao.getAllUsers()

    // Insert to Room (local)
    suspend fun insertUserLocal(user: User) {
        userDao.insertUser(user)
    }

    // Insert to Firebase (remote)
    suspend fun insertUserRemote(user: User) {
        userCollection.document(user.id).set(user).await()
    }

    // Delete from Room (local)
    suspend fun deleteUserLocal(user: User) {
        userDao.deleteUser(user)
    }

    // Delete from Firebase (remote)
    suspend fun deleteUserRemote(userId: String) {
        userCollection.document(userId).delete().await()
    }
}
