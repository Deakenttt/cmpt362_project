package com.example.matchmakers.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmakers.adapter.RecommendedUserCache
import com.example.matchmakers.model.User
import com.example.matchmakers.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class HomeViewModel(private val userViewModel: UserViewModel) : ViewModel() {

    private val likedUsers = MutableLiveData<List<User>>()
    private val userCache = RecommendedUserCache() // Local cache for recommended users
    private val _currentRecommendedUser = MutableLiveData<User?>() // Holds the currently displayed recommended user
    val currentRecommendedUser: LiveData<User?> get() = _currentRecommendedUser

    private val _errorMessage = MutableLiveData<String?>() // Holds error messages
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        loadRecommendedUsers()
    }

    /**
     * Fetch recommended users via `UserViewModel` and populate the cache.
     */
    private fun loadRecommendedUsers() {
        viewModelScope.launch {
            try {
                userViewModel.fetchRecommendedUsersForCurrentUser().join() // Trigger the fetching in UserViewModel
                val recommendedUsers = userViewModel.recommendedUsers.value ?: emptyList()

                if (recommendedUsers.isNotEmpty()) {
                    userCache.setUsers(recommendedUsers)
                    _currentRecommendedUser.value = userCache.getNextUser()

                    _errorMessage.value = null
                } else {
                    _currentRecommendedUser.value = null
                    _errorMessage.value = "No recommended users available."
                }
            } catch (e: Exception) {
                _currentRecommendedUser.value = null
                _errorMessage.value = "Failed to load recommended users: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Display the next user in the cache.
     */
    fun showNextUser() {
        _currentRecommendedUser.value = userCache.getNextUser() ?: run {
            userCache.resetIndex()
            userCache.getNextUser()
        }
    }

    /**
     * Like action placeholder - additional logic for liking can be added here.
     */
    fun likeUser() {
        // Logic for liking a user can be added here if needed
        showNextUser()
    }

    /**
     * Dislike action - move to the next user.
     */
    fun dislikeUser() {
        showNextUser()
    }

    /**
     * Refresh the cache via `UserViewModel` if needed.
     */
    fun refreshRecommendedUsers() {
        viewModelScope.launch {
            try {
                userViewModel.fetchRecommendedUsersForCurrentUser().join()
                val updatedUsers = userViewModel.recommendedUsers.value ?: emptyList()
                userCache.reload(updatedUsers)
                _currentRecommendedUser.value = userCache.getNextUser()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh recommended users: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Get liked users to display in MatchListFragment.
     */
    fun getLikedUsers(): LiveData<List<User>> {
        return likedUsers
    }
}
