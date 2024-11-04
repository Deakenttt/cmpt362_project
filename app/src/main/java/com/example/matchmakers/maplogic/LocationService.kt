package com.example.matchmakers.maplogic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LocationService(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(callback: (Location?) -> Unit) {
        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            callback(location)
        }
        locationTask.addOnFailureListener {
            callback(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location == null){
                println("location was null")
                return@addOnSuccessListener
            }
            val latitude = location.latitude
            val longitude = location.longitude
            println("location was $latitude, $longitude")
            saveLocationToFirestore(latitude, longitude)
        }
        locationTask.addOnFailureListener {

        }
    }

    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {
        val userId = auth.currentUser?.uid ?: return // Ensure the user is authenticated

        // Create a map to hold the location data
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )

        // Store location data
        db.collection("users").document(userId)
            .update(locationData)
            .addOnSuccessListener {
                println("mgs: $latitude $longitude")
                println("mgs: Location saved successfully")
            }
            .addOnFailureListener {
                println("mgs: Location write failed")
            }
    }



}
