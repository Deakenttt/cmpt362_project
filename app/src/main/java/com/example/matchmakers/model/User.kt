package com.example.matchmakers.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val interest: String
)