package com.example.matchmakers.ui.CreateAccount

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
                "recommended" to emptyList<Int>()
            )

            db.collection("users").document(uid)
                .set(userInfo)
                .addOnSuccessListener {
                    _accountCreated.value = true
                }
                .addOnFailureListener {
                    _accountCreated.value = false
                }
        } else {
            _accountCreated.value = false
        }
    }
}
