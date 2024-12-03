package com.example.matchmakers.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmakers.model.User
import com.example.matchmakers.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeViewModel(private val userViewModel: UserViewModel) : ViewModel() {

    val loggedInUser = FirebaseAuth.getInstance()
    private val _mutualLikeStatus = MutableLiveData<Boolean?>()
    val mutualLikeStatus: LiveData<Boolean?> get() = _mutualLikeStatus

    // Exposing the currently displayed user from UserViewModel
    val currentRecommendedUser: LiveData<User?> get() = userViewModel.currentUser

    val recommendedUserList: LiveData<List<User>> get() = userViewModel.recommendedUsers

    // Holds error messages for the UI
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val db = FirebaseFirestore.getInstance()

    init {
        // Ensure the first user is displayed on initialization
        userViewModel.displayFirstUser()
    }

    /**
     * Handles the "like" action for the current user.
     * Moves to the next user without removing the current one from the cache.
     */
    fun likeUser() {
        val currentUser = currentRecommendedUser.value
        val loggedInUserUid = loggedInUser.currentUser?.uid

        if (currentUser == null || loggedInUserUid == null) {
            _errorMessage.value = "No user to like or user not logged in."
            return
        }

        if(recommendedUserList.value.isNullOrEmpty()){
            _errorMessage.value = "mgs: Sorry, no more users available."
            return
        }

        viewModelScope.launch {
            try {
                userViewModel.handleLike()

                db.collection("profileinfo").document(currentUser.id).get()
                    .addOnSuccessListener { document ->
                        val likeList = document.get("LikeList") as? List<String>
                        if (likeList != null && loggedInUserUid in likeList) {
                            _mutualLikeStatus.value = true

                            val participants = listOf(loggedInUserUid, currentUser.id).sorted()
                            db.collection("conversations")
                                .whereArrayContains("participants", participants[0])
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val existingConversation = querySnapshot.documents.find { doc ->
                                        val conversationParticipants = doc.get("participants") as? List<String>
                                        conversationParticipants?.sorted() == participants
                                    }

                                    if (existingConversation == null) {
                                        val messages = mutableListOf<Map<String, String>>()

                                        val conversationData = hashMapOf(
                                            "participants" to participants,
                                            "messages" to messages
                                        )

                                        db.collection("conversations").add(conversationData)
                                            .addOnSuccessListener {
                                                _errorMessage.value = null
                                            }
                                            .addOnFailureListener { e ->
                                                _errorMessage.value = "Failed to create conversation: ${e.localizedMessage}"
                                            }
                                    } else {
                                        _errorMessage.value = "Conversation already exists."
                                    }
                                }
                                .addOnFailureListener { e ->
                                    _errorMessage.value = "Failed to check existing conversations: ${e.localizedMessage}"
                                }
                        } else {
                            _mutualLikeStatus.value = false
                        }
                    }
                    .addOnFailureListener { e ->
                        _errorMessage.value = "Failed to retrieve LikeList: ${e.localizedMessage}"
                    }

                _errorMessage.value = null // Clear any error messages
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like user: ${e.localizedMessage}"
            }
        }
    }


    /**
     * Handles the "dislike" action for the current user.
     * Removes the current user from the cache and displays the next one.
     */
    fun dislikeUser() {
        val currentUser = currentRecommendedUser.value
        if (currentUser == null) {
            _errorMessage.value = "No user to dislike."
            return
        }
        viewModelScope.launch {
            try {
                userViewModel.handleDislike() // Call UserViewModel's dislike handler
                _errorMessage.value = null // Clear any error messages
            } catch (e: Exception) {
                _errorMessage.value = "Failed to dislike user: ${e.localizedMessage}"
            }
        }
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
    /**
     * get the matches users
     */
    fun getLikedUsers() {
        return
    }
}
