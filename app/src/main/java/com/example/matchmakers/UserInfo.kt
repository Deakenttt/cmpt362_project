package com.example.matchmakers

data class UserInfo(
    val name: String = "",
    val interest: String = "",
    val gender: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0


)

data class ProfileInfo(
    var name: String = "",
    var age: Int = 0,
    var interests: List<String> = listOf(), // Change to multiple interests later
    var biography: String = ""
)