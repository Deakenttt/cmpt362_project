package com.example.matchmakers.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmakers.model.User
import com.example.matchmakers.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class HomeViewModel(private val userViewModel: UserViewModel) : ViewModel() {

    // Exposing the currently displayed user from UserViewModel
    val currentRecommendedUser: LiveData<User?> get() = userViewModel.currentUser


    // Holds error messages for the UI
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        // Ensure the first user is displayed on initialization
        userViewModel.displayFirstUser()
    }

    /**
     * Handles the "like" action for the current user.
     * Moves to the next user without removing the current one from the cache.
     */
    fun likeUser() {
        userViewModel.displayNextUser()
    }

    /**
     * Handles the "dislike" action for the current user.
     * Removes the current user from the cache and displays the next one.
     */
    fun dislikeUser() {
        userViewModel.deleteCurrentUser()
    }

    /**
     * Refresh the display of users when the cache updates.
     * Reloads the current user or resets to the first user if the list changes.
     */
    fun refreshRecommendedUsers() {
        try {
            userViewModel.refreshDisplay()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to refresh recommended users: ${e.localizedMessage}"
        }
    }

    /**
     * Loads the recommended users from the UserViewModel cache.
     * Displays the first user or shows an error message if the cache is empty.
     */
    private fun loadRecommendedUsers() {
        viewModelScope.launch {
            try {
                userViewModel.refreshDisplay() // Ensure display is in sync with the cache
                if (userViewModel.recommendedUsers.value.isNullOrEmpty()) {
                    _errorMessage.value = "No recommended users available."
                } else {
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recommended users: ${e.localizedMessage}"
            }
        }
    }
}
