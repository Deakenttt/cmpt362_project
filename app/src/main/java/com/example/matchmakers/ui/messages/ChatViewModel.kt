package com.example.matchmakers.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchmakers.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: MutableLiveData<List<ChatMessage>> get() = _messages

    private val _sendMessage = MutableLiveData<String>()
    val sendMessage: LiveData<String> get() = _sendMessage

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

    fun stringToChatMessage(
        attachments: String,
        type: String,
        receiverId: String,
        senderId: String,
        message: String,
        timestamp: String
    ): ChatMessage {
        return ChatMessage(
            attachments = attachments,
            messageType = type,
            receiverId = receiverId,
            senderId = senderId,
            message = message,
            timestamp = timestamp
        )
    }

    fun sendMessage(message: ChatMessage, conversationId: String) {
        val conversationRef = db.collection("conversations").document(conversationId)

        // Convert the ChatMessage object to a Map
        val messageData = mapOf(
            "attachments" to message.attachments,
            "message_type" to message.messageType,
            "receiverid" to message.receiverId,
            "senderid" to message.senderId,
            "text" to message.message,
            "timestamp" to message.timestamp
        )

        // Add the message to the messages array in the conversation document
        conversationRef.update("messages", FieldValue.arrayUnion(messageData))
            .addOnSuccessListener {
                _sendMessage.value = "Message sent successfully"
            }
            .addOnFailureListener { e ->
                _sendMessage.value = "Failed to send message: ${e.message}"
            }
    }

    fun getCurrentTimestamp(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}
