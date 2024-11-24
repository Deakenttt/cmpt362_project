package com.example.matchmakers.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    @PropertyName("id") // Maps Firebase field "id" to Room's `id`
    val id: String = "",

    @PropertyName("name") // Maps Firebase field "name"
    val name: String = "",

    @PropertyName("age") // Maps Firebase field "age"
    val age: Int = 0,

    @PropertyName("interest") // Maps Firebase field "interest"
    val interest: String = "",

    @PropertyName("lastMessage") // Maps Firebase field "lastMessage"
    val lastMessage: String = "",

    @PropertyName("conversationId") // Maps Firebase field "conversationId"
    val conversationId: String = "",

    @PropertyName("cluster") // Maps Firebase field "cluster" for user recommendation
    val cluster: Int = 0,

    // Timestamp of when the user was last updated in Firebase
    val lastUpdated: Long = 0L,

    // Room-only field to track when the user data was last fetched
    val lastFetched: Long = System.currentTimeMillis()
)
