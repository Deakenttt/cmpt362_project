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

    fun createAccount(name: String, interest: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val userInfo = hashMapOf(
                "name" to name,
                "interest" to interest
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
