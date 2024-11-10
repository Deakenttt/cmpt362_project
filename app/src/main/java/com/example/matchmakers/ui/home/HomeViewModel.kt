// HomeViewModel.kt
package com.example.matchmakers.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmakers.adapter.RecommendedUserCache
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val userCache = RecommendedUserCache() // Acts as a local cache of recommended users
    private val _currentUser = MutableLiveData<User?>() // Holds the currently displayed user
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        loadRecommendedUsers() // Load users when ViewModel is initialized
    }

    // Load recommended users from the repository and populate the cache
    private fun loadRecommendedUsers() {
        viewModelScope.launch {
            val recommendedUsers = userRepository.getRecommendedUsersForCurrentUser() // Fetch recommended users
            if (recommendedUsers.isNotEmpty()) {
                userCache.setUsers(recommendedUsers) // Load into cache
                _currentUser.value = userCache.getNextUser() // Start by displaying the first user
            } else {
                _currentUser.value = null // Set to null if no users are available
            }
        }
    }

    // Display the next user in the cache
    fun showNextUser() {
        _currentUser.value = userCache.getNextUser() ?: run {
            userCache.resetIndex() // Reset to the beginning if we reached the end
            userCache.getNextUser()
        }
    }

    // Like action placeholder - additional logic for "like" can be added here
    fun likeUser() {
        // This is a placeholder for any specific logic you want to add for "liking" a user
    }

    // Dislike action - move to the next user
    fun dislikeUser() {
        showNextUser()
    }

    // Refresh the cache from the repository if needed
    fun refreshRecommendedUsers() {
        viewModelScope.launch {
            val updatedUsers = userRepository.getRecommendedUsersForCurrentUser()
            userCache.reload(updatedUsers) // Refresh cache
            _currentUser.value = userCache.getNextUser() // Display the first user in the updated cache
        }
    }
}
