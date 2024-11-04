package com.example.matchmakers.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun register(email: String, password: String) {
        if (email.isEmpty()) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        // Check for valid email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (password.isEmpty()) {
            _errorMessage.value = "Please enter a password."
            return
        }
        // Check password length
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            return
        }

        if (password.length > 20) {
            _errorMessage.value = "Password must not exceed 20 characters."
            return
        }
        // Try to register the user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registrationResult.value = true
                } else {
                    // Get specific FirebaseAuth error message if registration fails
                    val exception = task.exception
                    val message = if (exception is FirebaseAuthException) {
                        getFirebaseAuthErrorMessage(exception)
                    } else {
                        "Registration failed. Please try again."
                    }
                    _errorMessage.value = message
                    _registrationResult.value = false
                }
            }
    }
    // Function to map Firebase Auth error codes to user-friendly messages
    private fun getFirebaseAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email address is already in use."
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_WEAK_PASSWORD" -> "The password is too weak. It should be at least 6 characters."
            else -> "Registration failed. Please try again."
        }
    }
}
