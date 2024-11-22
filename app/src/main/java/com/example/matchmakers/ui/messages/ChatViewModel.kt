package com.example.matchmakers.ui.messages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchmakers.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: MutableLiveData<List<ChatMessage>> get() = _messages

    fun fetchMessages(conversationId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val conversations = db.collection("conversations")
            conversations.document(conversationId)
                .get()
                .addOnSuccessListener { conversation ->
                    if (conversation.exists()) {
                        val messages = conversation.get("messages") as? List<Map<String, Any>>
                        println("mgs: message value: $messages")
                        if (messages != null) {
                            _messages.value = messages.mapNotNull { map ->
                                mapToChatMessage(map)
                            }
                        } else {
                            _messages.value = emptyList()
                        }
                    }
                }
        }
    }

    private fun mapToChatMessage(map: Map<String, Any>): ChatMessage? {
        println("mgs: Map contents: $map ")
        return try {
            ChatMessage(
                attachments = map["attachments"] as? String ?: "",
                messageType = map["message_type"] as? String ?: "",
                receiverId = map["receiverid"] as? String ?: "",
                senderId = map["senderid"] as? String ?: "",
                message = map["text"] as? String ?: "",
                timestamp = map["timestamp"] as? String ?: ""
            )
        } catch (e: Exception) {
            println("mgs: Exception $e")
            null
        }
    }
}
