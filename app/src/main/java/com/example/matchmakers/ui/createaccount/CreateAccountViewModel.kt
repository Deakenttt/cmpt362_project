package com.example.matchmakers.ui.createaccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _accountCreated = MutableLiveData<Boolean>()
    val accountCreated: LiveData<Boolean> = _accountCreated

    fun createAccount(name: String, age: Int, interest: String, gender: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val userInfo = hashMapOf(
                "name" to name,
                "age" to age,
                "interest" to interest,
                "gender" to gender,
                "latitude" to 0.0,
                "longitude" to 0.0,
                "cluster" to 0,
                "recommended" to emptyList<Int>(),
                "matches" to emptyList<String>() // Stores UIDs of matched users

            )

            db.collection("users").document(uid)
                .set(userInfo)
                .addOnSuccessListener {
                    _accountCreated.value = true
                }
                .addOnFailureListener {
                    _accountCreated.value = false
                }

            val profileInfo = hashMapOf(
                "name" to name,
                "interest" to interest,
                "age" to age
            )

            // Update profileinfo collection to store all insensitive user data
            db.collection("profileinfo").document(uid)
                .set(profileInfo)

        } else {
            _accountCreated.value = false
        }
    }
}
