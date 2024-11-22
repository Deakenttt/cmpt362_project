package com.example.matchmakers.model

data class ChatMessage(
    val attachments: String,
    val messageType: String,
    val receiverId: String,
    val senderId: String,
    val message: String,
    val timestamp: String
)