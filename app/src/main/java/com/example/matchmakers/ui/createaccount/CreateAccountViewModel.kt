package com.example.matchmakers.ui.createaccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                // These are already stored in profileinfo so they will not be stored here in users
                //"name" to name,
                //"age" to age,
                //"interest" to interest,
                //"gender" to gender,
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

            val calendar = Calendar.getInstance()
            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val profileInfo = hashMapOf(
                "name" to name,
                "age" to age,
                "gender" to gender,
                "interest1" to interest,
                "interest2" to "",
                "interest3" to "",
                "images" to listOf<String>(),
                "biography" to "",
                "timestamp" to timestamp
            )

            // Update profileinfo collection to store all insensitive user data
            db.collection("profileinfo").document(uid)
                .set(profileInfo)

        } else {
            _accountCreated.value = false
        }
    }
}
