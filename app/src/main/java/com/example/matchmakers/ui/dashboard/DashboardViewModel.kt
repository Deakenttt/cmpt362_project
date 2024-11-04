package com.example.matchmakers.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchmakers.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.core.UserData
import com.google.firebase.firestore.firestore

class DashboardViewModel : ViewModel() {

//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    private val db: FirebaseFirestore = Firebase.firestore
//
//    private val _name = MutableLiveData<String>()
//    val name: LiveData<String> = _name
//
//    private val _interest = MutableLiveData<String>()
//    val interest: LiveData<String> = _interest
//
//    init {
//        fetchUserData()
//    }
//
//    private fun fetchUserData() {
//        val userId = auth.currentUser?.uid
//        if(userId != null) {
//            db.collection("users")
//                .document(userId)
//                .get()
//                .addOnSuccessListener { document ->
//                    if(document != null) {
//                        _name.value = document.getString("name")
//                        _interest.value = document.getString("interest")
//                    } else {
//                        // Handle case where document doesn't exist
//                        _name.value = "Name not found"
//                        _interest.value = "Interest not found"
//                    }
//                }
//                .addOnFailureListener {
//                    // Handle error
//                }
//        } else {
//            // User not authenticated, handle error
//        }
//    }
//
//    private val _text = MutableLiveData<String>().apply {
//        value = "This is dashboard Fragment"
//    }
//    val text: LiveData<String> = _text
}