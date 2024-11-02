package com.example.matchmakers.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun register(email: String, password: String) {
        if (email.isEmpty()) {
            _errorMessage.value = "Enter email"
            return
        }
        if (password.isEmpty()) {
            _errorMessage.value = "Enter password"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _registrationResult.value = task.isSuccessful
                if (!task.isSuccessful) {
                    _errorMessage.value = "Registration failed."
                }
            }
    }
}
