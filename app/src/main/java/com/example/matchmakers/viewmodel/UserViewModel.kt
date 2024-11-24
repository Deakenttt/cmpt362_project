package com.example.matchmakers.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.LocalUserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: LocalUserRepository) : ViewModel() {

    private val TAG = "UserViewModel"

    // LiveData to observe the full list of recommended users from the cache
    val recommendedUsers: LiveData<List<User>> = repository.getAllRecommendedUsers().asLiveData()

    // LiveData to observe the currently displayed user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    // Internal index to keep track of the current user in the list
    private var currentIndex = 0

    init {
        Log.d(TAG, "UserViewModel initialized. Observing recommended users from cache.")
        recommendedUsers.observeForever { users ->
            if (users.isEmpty()) {
                Log.d(TAG, "Recommended users cache is empty.")
            } else {
                Log.d(TAG, "Recommended users cache contains ${users.size} users.")
            }
        }
    }

    /**
     * Displays the first user from the recommended users list.
     * Called during initialization or when resetting the display.
     */
    fun displayFirstUser() {
        recommendedUsers.value?.let { users ->
            if (users.isNotEmpty()) {
                currentIndex = 0
                _currentUser.value = users[currentIndex]
                Log.d(TAG, "Displaying first user: ${users[currentIndex].id}")
            } else {
                _currentUser.value = null // No users available
                Log.d(TAG, "No users available to display.")
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
                Log.d(TAG, "Displaying next user: ${users[currentIndex].id}")
            } else {
                _currentUser.value = null // No more users to display
                Log.d(TAG, "No more users to display. Resetting display.")
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
            Log.d(TAG, "Displaying previous user: ${_currentUser.value?.id}")
        } else {
            Log.d(TAG, "Already at the first user. Cannot move to previous user.")
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
                Log.d(TAG, "Refreshing display for user: ${users[currentIndex].id}")
            } else if (users.isNotEmpty()) {
                currentIndex = 0
                _currentUser.value = users[currentIndex]
                Log.d(TAG, "Refreshing display. Reset to first user: ${users[currentIndex].id}")
            } else {
                _currentUser.value = null // No users available
                Log.d(TAG, "Refreshing display. No users available.")
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
                Log.d(TAG, "Deleting user with ID: ${it.id}")
                repository.deleteUserById(it.id)
                Log.d(TAG, "Deleted user with ID: ${it.id}. Moving to next user.")
                displayNextUser() // Automatically move to the next user
            }
        } ?: Log.d(TAG, "No user to delete.")
    }
}
