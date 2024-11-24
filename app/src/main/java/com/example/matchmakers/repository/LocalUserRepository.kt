package com.example.matchmakers.repository

import com.example.actiontabskotlin.database.UserDao
import com.example.matchmakers.model.User
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val userDao: UserDao) {

    fun getAllRecommendedUsers(): Flow<List<User>> = userDao.getAllRecommendedUsers()

    suspend fun insertRecommendedUsers(users: List<User>) {
        userDao.insertRecommendedUsers(users)
    }

    suspend fun insertRecommendedUser(user: User) {
        userDao.insertRecommendedUser(user)
    }

    suspend fun deleteAllRecommendedUsers() {
        userDao.deleteAllRecommendedUsers()
    }

    suspend fun deleteUserById(userId: String) {
        userDao.deleteUserById(userId)
    }

    suspend fun getLastFetchedTimestamp(): Long? = userDao.getLastFetchedTimestamp()

    suspend fun updateLastFetchedTimestampForAll(timestamp: Long) {
        userDao.updateLastFetchedTimestampForAll(timestamp)
    }
}
