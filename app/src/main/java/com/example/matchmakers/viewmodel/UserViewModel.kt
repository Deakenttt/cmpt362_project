package com.example.matchmakers.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.matchmakers.domain.UserService
import com.example.matchmakers.model.User
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.repository.RemoteUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: LocalUserRepository,
    private val remoteRepository: RemoteUserRepository
) : ViewModel() {

    private val TAG = "UserViewModel"

    // LiveData to observe the full list of recommended users from the cache
    val recommendedUsers: LiveData<List<User>> = repository.getAllRecommendedUsers().asLiveData()

    // LiveData to observe the currently displayed user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    private val db = FirebaseFirestore.getInstance()

    // Internal index to keep track of the current user in the list
    private var currentIndex = 0

//    init {
//        Log.d(TAG, "UserViewModel initialized. Observing recommended users from cache.")
//        recommendedUsers.observeForever { users ->
//            if (users.isEmpty()) {
//                Log.d(TAG, "Recommended users cache is empty. No users to display.")
//                _currentUser.value = null
//                currentIndex = 0
//            } else {
//                Log.d(TAG, "Recommended users cache contains ${users.size} users.")
//                currentIndex = 0
//                _currentUser.value = users.getOrNull(currentIndex)
//            }
//        }
//    }

    init {
        Log.d(TAG, "UserViewModel initialized. Forcing cache refresh.")

        // Force a cache refresh before observing LiveData
        viewModelScope.launch {
            forceCacheRefresh()
            observeLiveData()
        }
    }

    /**
     * Force cache refresh before observing LiveData.
     */
    private suspend fun forceCacheRefresh() {
        val userService = UserService(repository, remoteRepository)
        userService.getRecommendedUsers() // Fetch and update the cache
        Log.d(TAG, "Cache refresh forced in UserViewModel initialization.")
    }

    /**
     * Observe recommended users LiveData and synchronize with cache.
     */
    private fun observeLiveData() {
        recommendedUsers.observeForever { users ->
            if (users.isEmpty()) {
                viewModelScope.launch {
                    val cacheSize = repository.getCacheSize()
                    Log.d(TAG, "Recommended users cache is empty. The actual cache size is $cacheSize.")
                }
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

            // Move to the next user in the list
            currentIndex++
            if (currentIndex < users.size) {
                _currentUser.value = users[currentIndex]
                Log.d(TAG, "Displaying next user: ID = ${users[currentIndex].id}, Name = ${users[currentIndex].name}")
            } else {
                // If we've reached the end, set current user to null
                _currentUser.value = null
                Log.d(TAG, "No more users to display. Reached the end of the cache.")
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
        userToDelete?.let { user ->
            viewModelScope.launch {
                Log.d(TAG, "Deleting user with ID: ${user.id}")

                // Delete the user from the cache (local repository)
                repository.deleteUserById(user.id)
                Log.d(TAG, "Deleted user with ID: ${user.id}")

                // Check the updated cache
                val updatedUsers = recommendedUsers.value ?: emptyList()
                if (updatedUsers.isEmpty()) {
                    // If no users are left in the cache, reset the current user
                    _currentUser.value = null
                    Log.d(TAG, "No more users to display. Cache is empty.")
                } else {
                    // Adjust index if needed
                    if (currentIndex >= updatedUsers.size) {
                        currentIndex = updatedUsers.size - 1 // Prevent index overflow
                    }

                    // Update the current user
                    _currentUser.value = updatedUsers[currentIndex]
                    Log.d(TAG, "Displaying next user after deletion: ID = ${_currentUser.value?.id}, Name = ${_currentUser.value?.name}")
                }
            }
        } ?: Log.d(TAG, "No user to delete.")
    }

    /**
     * Handles the "Like" action.
     */
    fun handleLike() {
        val loggedInUserId = remoteRepository.getCurrentUserId()
        val likedUser = _currentUser.value

        if (loggedInUserId == null || likedUser == null) {
            Log.e(TAG, "Cannot handle like. Missing logged-in user ID or current user.")
            return
        }

        viewModelScope.launch {
            try {
                // Perform Like action using Remote Repository
                remoteRepository.handleLikeAction(loggedInUserId, likedUser.id)
                Log.d(TAG, "User ${likedUser.id} liked successfully.")

                // Delete the user from the local cache and display the next user
                deleteCurrentUser()
                displayNextUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like action: ${e.message}", e)
            }
        }
    }

    /**
     * Handles the "Dislike" action.
     */
    fun handleDislike() {
        val loggedInUserId = remoteRepository.getCurrentUserId()
        val dislikedUser = _currentUser.value

        if (loggedInUserId == null || dislikedUser == null) {
            Log.e(TAG, "Cannot handle dislike. Missing logged-in user ID or current user.")
            return
        }

        viewModelScope.launch {
            try {
                // Perform Dislike action using Remote Repository
                remoteRepository.handleDislikeAction(loggedInUserId, dislikedUser.id)
                Log.d(TAG, "User ${dislikedUser.id} disliked successfully.")

                // Delete the user from the local cache and display the next user
                deleteCurrentUser()
                displayNextUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling dislike action: ${e.message}", e)
            }
        }
    }


}
