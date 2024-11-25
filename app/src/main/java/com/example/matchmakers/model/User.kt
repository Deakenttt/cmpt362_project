package com.example.matchmakers.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    val id: String = "", // Document ID from Firebase

    val name: String = "", // User's name

    val age: Int = 0, // User's age

    val interest: String = "", // User's interest(s)

    val lastMessage: String = "", // Placeholder for the last message

    val conversationId: String = "", // Conversation ID related to the user

    val cluster: Int = 0, // Cluster ID for recommendations

    // Timestamp of when the user was last updated in Firebase
    val lastUpdated: Long = 0L,

    // Room-only field to track when the user data was last fetched
    val lastFetched: Long = System.currentTimeMillis()
)

