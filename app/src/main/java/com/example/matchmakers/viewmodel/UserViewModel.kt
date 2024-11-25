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
                Log.d(TAG, "Recommended users cache is empty. No users to display.")
                _currentUser.value = null
                currentIndex = 0
            } else {
                Log.d(TAG, "Recommended users cache contains ${users.size} users.")
                currentIndex = 0
                _currentUser.value = users.getOrNull(currentIndex)
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
        }?: Log.d(TAG, "Recommended users LiveData is null.")
    }

    /**
     * Moves to the next user in the recommended users list.
     * If the list has no more users, sets the current user to null.
     */
    fun displayNextUser() {
        recommendedUsers.value?.let { users ->
            if (users.isEmpty()) {
                Log.d(TAG, "Recommended users cache is empty. No users to display.")
                _currentUser.value = null
                return
            }

            if (currentIndex < users.size - 1) {
                // Increment index and display the next user
                currentIndex++
                _currentUser.value = users[currentIndex]
                Log.d(TAG, "Displaying next user: ID = ${users[currentIndex].id}, Name = ${users[currentIndex].name}")
            } else {
                // Stop when the last user is reached
                Log.d(TAG, "No more users to display. Reached the last user in the cache.")
                _currentUser.value = null
            }
        } ?: run {
            _currentUser.value = null
            Log.d(TAG, "Recommended users LiveData is null. Cannot display next user.")
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

                // Delete user from the cache
                repository.deleteUserById(it.id)
                Log.d(TAG, "Deleted user with ID: ${it.id}")

                // Fetch the updated cache after deletion
                val updatedUsers = recommendedUsers.value ?: emptyList()
                Log.d(TAG, "Recommended users cache contains ${updatedUsers.size} users after deletion.")

                if (updatedUsers.isNotEmpty()) {
                    // Adjust index to the next user in the list
                    currentIndex = updatedUsers.indexOfFirst { user -> user.id == it.id }
                    if (currentIndex != -1 && currentIndex < updatedUsers.size - 1) {
                        // Move to the next user
                        currentIndex++
                    } else {
                        // If the deleted user was the last, move to the new last user
                        currentIndex = updatedUsers.size - 1
                    }

                    // Update the current user
                    _currentUser.value = updatedUsers.getOrNull(currentIndex)
                    Log.d(TAG, "Displaying next user: ID = ${_currentUser.value?.id}, Name = ${_currentUser.value?.name}")
                } else {
                    // No users left in the cache
                    _currentUser.value = null
                    currentIndex = 0
                    Log.d(TAG, "No more users to display. Cache is empty.")
                }
            }
        } ?: Log.d(TAG, "No user to delete.")
    }


}
