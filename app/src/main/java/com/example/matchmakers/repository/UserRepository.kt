package com.example.matchmakers.repository

import com.example.actiontabskotlin.database.UserDao
import com.example.matchmakers.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userCollection = firebaseDb.collection("users")

    // Fetch from Room (local)
    fun getUsersByInterestLocal(interest: String): Flow<List<User>> = userDao.getUsersByInterest(interest)
    fun getAllUsersLocal(): Flow<List<User>> = userDao.getAllUsers()

    // Fetch from Firebase (remote) with placeholder for filtering
    suspend fun getUsersByInterestRemote(interest: String): List<User> {
        return try {
            userCollection.whereEqualTo("interest", interest).get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()  // Handle error, return empty list as placeholder
        }
    }

    // Insert to Room (local)
    suspend fun insertUserLocal(user: User) {
        userDao.insertUser(user)
    }

    // Insert to Firebase (remote) with placeholder for adding data
    suspend fun insertUserRemote(user: User) {
        userCollection.document(user.id).set(user).await()
    }

    // Delete from Room (local)
    suspend fun deleteUserLocal(user: User) {
        userDao.deleteUser(user)
    }

    // Delete from Firebase (remote) with placeholder for deletion
    suspend fun deleteUserRemote(userId: String) {
        userCollection.document(userId).delete().await()
    }
}
