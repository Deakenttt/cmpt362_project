package com.example.matchmakers.viewmodel

import androidx.lifecycle.*
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.LocalUserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: LocalUserRepository) : ViewModel() {

    // LiveData to observe the full list of recommended users from the cache
    val recommendedUsers: LiveData<List<User>> = repository.getAllRecommendedUsers().asLiveData()

    // LiveData to observe the currently displayed user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    // Internal index to keep track of the current user in the list
    private var currentIndex = 0

    /**
     * Displays the first user from the recommended users list.
     * Called during initialization or when resetting the display.
     */
    fun displayFirstUser() {
        recommendedUsers.value?.let { users ->
            if (users.isNotEmpty()) {
                currentIndex = 0
                _currentUser.value = users[currentIndex]
            } else {
                _currentUser.value = null // No users available
            }
        }
    }

    /**
     * Moves to the next user in the recommended users list.
     * If the list has no more users, sets the current user to null.
     */
    fun displayNextUser() {
        recommendedUsers.value?.let { users ->
            if (currentIndex < users.size - 1) {
                currentIndex++
                _currentUser.value = users[currentIndex]
            } else {
                _currentUser.value = null // No more users to display
            }
        }
    }

    /**
     * Moves to the previous user in the recommended users list.
     * If already at the first user, does nothing.
     */
    fun displayPreviousUser() {
        if (currentIndex > 0) {
            currentIndex--
            _currentUser.value = recommendedUsers.value?.get(currentIndex)
        }
    }

    /**
     * Refreshes the displayed users when the cache updates.
     * Keeps the currentIndex in sync with the latest data.
     */
    fun refreshDisplay() {
        recommendedUsers.value?.let { users ->
            if (users.isNotEmpty() && currentIndex < users.size) {
                _currentUser.value = users[currentIndex]
            } else if (users.isNotEmpty()) {
                currentIndex = 0
                _currentUser.value = users[currentIndex]
            } else {
                _currentUser.value = null // No users available
            }
        }
    }

    /**
     * Deletes the currently displayed user from the cache. (dislike this user)
     * After deletion, moves to the next user.
     */
    fun deleteCurrentUser() {
        val userToDelete = _currentUser.value
        userToDelete?.let {
            viewModelScope.launch {
                repository.deleteUserById(it.id)
                displayNextUser() // Automatically move to the next user
            }
        }
    }
}
