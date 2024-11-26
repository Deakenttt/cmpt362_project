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
    var gender: String = "",
    var interest1: String = "",
    var interest2: String = "",
    var interest3: String = "",
    var biography: String = "",
    var timestamp: String = ""
)