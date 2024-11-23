package com.example.matchmakers.adapter

import com.example.matchmakers.model.User

//RecommendedUserCache will serve as a cache for managing recommended users
class RecommendedUserCache {
    private val userList = mutableListOf<User>()
    private var currentIndex = 0

    // Load recommended users into the cache
    @Synchronized
    fun setUsers(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        currentIndex = 0
    }

    // Get the next user to display
    @Synchronized
    fun getNextUser(): User? {
        return if (currentIndex < userList.size) userList[currentIndex++] else null
    }

    // Reload the cache when needed
    @Synchronized
    fun reload(users: List<User>) {
        setUsers(users)
    }

    // Check if the cache is empty
    @Synchronized
    fun isEmpty(): Boolean = userList.isEmpty()

    // Reset index to start from the beginning
    @Synchronized
    fun resetIndex() {
        currentIndex = 0
    }
}
