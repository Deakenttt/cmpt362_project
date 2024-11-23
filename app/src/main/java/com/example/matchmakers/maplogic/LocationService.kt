package com.example.matchmakers.maplogic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationService(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    //cache location
    private var lastKnownLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(callback: (Location?) -> Unit) {
        val cacheValid = lastKnownLocation != null &&
                (System.currentTimeMillis() - lastKnownLocation!!.time) < 60 * 60 * 1000 // 1 hour

        if (cacheValid) {
            callback(lastKnownLocation)
        } else {
            // if cache is not valid, fetch new location
            getLocation(callback)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(callback: (Location?) -> Unit) {
        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                lastKnownLocation = location
                saveLocationToFirestore(location.latitude, location.longitude)
                callback(location)
            } else {
                // request location update if last known location is null
                requestLocationUpdate(callback)
            }
        }
        locationTask.addOnFailureListener {
            callback(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate(callback: (Location?) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // updatelocation every 10 seconds
            fastestInterval = 5000 // update location every 5 seconds
            numUpdates = 1
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    lastKnownLocation = location
                    saveLocationToFirestore(location.latitude, location.longitude)
                    callback(location)
                } else {
                    callback(null)
                }
                fusedLocationClient.removeLocationUpdates(this) //
            }
        }

        // Request a location update and set a timeout mechanism
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // stop location update requests after 30 seconds
        GlobalScope.launch {
            delay(30000)
            fusedLocationClient.removeLocationUpdates(locationCallback)
            callback(null)
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
        // Store location data
        db.collection("users").document(userId)
            .set(locationData, SetOptions.merge())
            .addOnSuccessListener {
                println("msg: $latitude, $longitude - Location saved successfully")
            }
            .addOnFailureListener {
                println("msg: Location write failed - ${it.message}")
            }
    }



}
