// RecommendedUserCache.kt
package com.example.matchmakers.adapter

import com.example.matchmakers.model.User

//RecommendedUserCache will serve as a cache for managing recommended users
class RecommendedUserCache {
    private val userList = mutableListOf<User>()
    private var currentIndex = 0

    // Load recommended users into the cache
    fun setUsers(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        currentIndex = 0
    }

    // Get the next user to display
    fun getNextUser(): User? {
        return if (currentIndex < userList.size) userList[currentIndex++] else null
    }

    // Reload the cache when needed
    fun reload(users: List<User>) {
        setUsers(users)
    }

    // Check if the cache is empty
    fun isEmpty(): Boolean = userList.isEmpty()

    // Reset index to start from the beginning
    fun resetIndex() {
        currentIndex = 0
    }
}
