package com.example.matchmakers.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun login(email: String, password: String) {
        if (email.isEmpty()) {
            _errorMessage.value = "Enter email"
            return
        }
        if (password.isEmpty()) {
            _errorMessage.value = "Enter password"
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _loginResult.value = task.isSuccessful
                if (!task.isSuccessful) {
                    _errorMessage.value = "Authentication failed."
                }
            }
    }

    fun checkProfile(registered: () -> Unit, unregistered: () -> Unit){
        val user = auth.currentUser ?: return
        val uid = user.uid
        // Checks if the user has completed their registration and set their profileinfo
        db.collection("profileinfo").document(uid).get().addOnSuccessListener {
            val name = it.getString("name")

            if (name != null){
                println("Registration completed")
                registered()
            } else{
                println("Registration not completed")
                unregistered()
            }
        }.addOnFailureListener{ error ->
            println(error)
        }
    }
}
