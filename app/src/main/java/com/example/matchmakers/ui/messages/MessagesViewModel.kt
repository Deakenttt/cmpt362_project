package com.example.matchmakers.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchmakers.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class MessagesViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _conversationIds = MutableLiveData<List<String>>()
    val conversationIds: LiveData<List<String>> get() = _conversationIds

    private val _lastMessage = MutableLiveData<String>()
    val lastMessage: LiveData<String> get() = _lastMessage

    private val _usersList = MutableLiveData<List<User>>()
    val usersList: LiveData<List<User>> get() = _usersList

    private val _messageReceiver = MutableLiveData<String?>()
    val messageReceiver: MutableLiveData<String?> get() = _messageReceiver

    // Function to fetch all matches (messages user has).
    fun fetchMatches() {
        val currentUser = auth.currentUser ?: return
        val currentUid = currentUser.uid

        db.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .get()
            .addOnSuccessListener { documents ->
                handleConversations(documents, currentUid)
            }
            .addOnFailureListener { exception ->
                println("mgs: Error getting documents: $exception")
            }
    }

    private fun handleConversations(documents: QuerySnapshot, currentUid: String) {
        val conversationIdsList = mutableListOf<String>()
        val usersList = mutableListOf<User>()

        if (documents.isEmpty) {
            println("mgs: No conversations found")
            return
        }

        for (document in documents) {
            val conversationId = document.id
            conversationIdsList.add(conversationId)

            val messageText = getLastMessageText(document)
            _lastMessage.value = messageText
            println("mgs: Last message text: $messageText")

            val participants = document.get("participants") as? List<String>
            participants?.let {
                getOtherUserDetails(it, currentUid, usersList, conversationId)
            }
        }

        // Update LiveData
        _conversationIds.value = conversationIdsList
    }

    private fun getLastMessageText(document: DocumentSnapshot): String {
        val messages = document.get("messages") as? List<Map<String, String>>
        return messages?.lastOrNull()?.get("text") ?: ""
    }

    private fun getOtherUserDetails(
        participants: List<String>,
        currentUid: String,
        usersList: MutableList<User>,
        conversationId: String
    ) {
        val otherUserUid = participants.firstOrNull { it != currentUid } ?: return
        println("mgs: OtherUserUid: $otherUserUid")

        db.collection("profileinfo").document(otherUserUid)
            .get()
            .addOnSuccessListener { userDoc ->
                val user = createUserFromDocument(userDoc, otherUserUid, conversationId)
                usersList.add(user)

                // Update the LiveData with the list of users
                _usersList.value = usersList

                println("mgs: Other user name: ${user.name}, Age: ${user.age}")
            }
            .addOnFailureListener { exception ->
                println("mgs: Error fetching user data: $exception")
            }
    }

    private fun createUserFromDocument(
        userDoc: DocumentSnapshot,
        otherUserUid: String,
        conversationId: String
    ): User {
        val userName = userDoc.getString("name") ?: "Unknown User"
        val age = userDoc.getLong("age")?.toInt() ?: 0
        val interest = userDoc.getString("interest") ?: "Unknown interest"

        return User(otherUserUid, userName, age, interest, _lastMessage.value!!, conversationId)
    }

    fun getMessageReceiverId(conversationId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _messageReceiver.value = "User is not authenticated"
            return
        }

        val currentUserUid = currentUser.uid
        val conversationRef = db.collection("conversations").document(conversationId)

        conversationRef.get()
            .addOnSuccessListener { conversation ->
                if (!conversation.exists()) {
                    _messageReceiver.value = "Conversation does not exist"
                    return@addOnSuccessListener
                }

                val participants = conversation.get("participants") as? List<String>
                if (participants == null) {
                    _messageReceiver.value = "Participants not found in conversation"
                    return@addOnSuccessListener
                }

                val otherUserUid = participants.find { it != currentUserUid }
                _messageReceiver.value = otherUserUid ?: "No other user found in conversation"
            }
            .addOnFailureListener { e ->
                _messageReceiver.value = "Failed to get conversation: ${e.message}"
            }
    }

}

