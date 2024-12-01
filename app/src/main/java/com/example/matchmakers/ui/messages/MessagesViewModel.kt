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

    private val _lastMessage = MutableLiveData<Map<String, String>>()
    val lastMessage: LiveData<Map<String, String>> get() = _lastMessage

    private val _usersList = MutableLiveData<List<User>>()
    val usersList: LiveData<List<User>> get() = _usersList

    fun fetchMatches() {
        val currentUser = auth.currentUser ?: return
        val currentUid = currentUser.uid

        db.collection("conversations")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    println("mgs: Error getting documents: $error")
                    return@addSnapshotListener
                }

                documents?.let {
                    handleConversations(it, currentUid)
                }
            }
    }

    private fun handleConversations(documents: QuerySnapshot, currentUid: String) {
        val conversationIdsList = mutableListOf<String>()
        val usersList = mutableListOf<User>()
        val lastMessageMap = mutableMapOf<String, String>()

        if (documents.isEmpty) {
            println("mgs: No conversations found")
            return
        }

        for (document in documents) {
            val conversationId = document.id
            conversationIdsList.add(conversationId)

            val messageText = getLastMessageText(document)
            lastMessageMap[conversationId] = messageText

            val participants = document.get("participants") as? List<String>
            participants?.let {
                getOtherUserDetails(it, currentUid, usersList, conversationId)
            }
        }

        _conversationIds.value = conversationIdsList
        _lastMessage.value = lastMessageMap
        println("mgs: last message value in viewmodel = ${_lastMessage.value}")
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

        db.collection("profileinfo").document(otherUserUid)
            .get()
            .addOnSuccessListener { userDoc ->
                val lastMessageText = _lastMessage.value?.get(conversationId) ?: ""
                val user = createUserFromDocument(userDoc, otherUserUid, conversationId, lastMessageText)
                synchronized(usersList) {
                    usersList.add(user)
                }

                if (usersList.size == _conversationIds.value?.size) {
                    _usersList.value = usersList
                }

                println("mgs: userList: $usersList")

            }
            .addOnFailureListener { exception ->
                println("mgs: Error fetching user data: $exception")
            }
    }

    private fun createUserFromDocument(
        userDoc: DocumentSnapshot,
        otherUserUid: String,
        conversationId: String,
        lastMessageText: String
    ): User {
        val userName = userDoc.getString("name") ?: "Unknown User"
        val age = userDoc.getLong("age")?.toInt() ?: 0
        val interest = userDoc.getString("interest") ?: "Unknown interest"

        return User(otherUserUid, userName, age, interest, lastMessageText, conversationId)
    }
}


