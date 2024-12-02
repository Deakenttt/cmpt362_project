package com.example.matchmakers.ui.messages

data class MessageUser(
    val id: String = "", // Document ID from Firebase

    val name: String = "", // User's name

    val age: Int = 0, // User's age

    val interest1: String = "", // User's interest(s)

    val interest2: String = "",

    val interest3: String = "",

    val lastMessage: String = "", // Placeholder for the last message

    val conversationId: String = "", // Conversation ID related to the user

)
