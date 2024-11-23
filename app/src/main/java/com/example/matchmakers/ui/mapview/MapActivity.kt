package com.example.matchmakers.ui.mapview

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.matchmakers.R
import com.example.matchmakers.maplogic.LocationService
import com.example.matchmakers.UserInfo
import com.example.matchmakers.mapslogic.MapDataHandler
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var locationService: LocationService
    private val mapDataHandler = MapDataHandler()
    private val firestore = FirebaseFirestore.getInstance() // Firebase Firestore
    private var userLocationListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initializeMap()

        val backButton = findViewById<Button>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeMap() {
        locationService = LocationService(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Show user's current location
        locationService.getLastKnownLocation { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(mapDataHandler.createMarkerOptions(userLocation, "You are here"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                // Save user's current location to Firestore
                saveLocationToFirestore(location.latitude, location.longitude)

                // Load other users' locations from Firebase
                loadOtherUsersFromFirebaseRealTime(userLocation)
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Ensure the user is authenticated

        // Create a map to hold the location data
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )

        // Store location data
        firestore.collection("users").document(userId)
            .set(locationData, SetOptions.merge())
            .addOnSuccessListener {
                println("msg: $latitude, $longitude - Location saved successfully")
            }
            .addOnFailureListener {
                println("msg: Location write failed - ${it.message}")
            }
    }

    private fun loadOtherUsersFromFirebaseRealTime(currentUserLocation: LatLng) {
        userLocationListener = firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to load user locations: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val users = snapshot.documents.mapNotNull { document ->
                        val name = document.getString("name") ?: "Unknown"
                        val interest = document.getString("interest") ?: "None"
                        val gender = document.getString("gender") ?: "Unknown"
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")

                        if (latitude != null && longitude != null) {
                            val userLocation = LatLng(latitude, longitude)
                            println("User: $name, Latitude: $latitude, Longitude: $longitude")
                            UserInfo(name, interest, gender, latitude, longitude)
                        } else {
                            null
                        }
                    }

                    googleMap.clear() // Clear existing markers
                    // Adding user markers to the map
                    users.forEach { user ->
                        googleMap.addMarker(mapDataHandler.createMarkerOptions(
                            LatLng(user.latitude, user.longitude),
                            user.name
                        ))
                    }
                } else {
                    Toast.makeText(this, "Failed to load user locations", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Determine if the user is nearby, defined as within a radius of 1 kilometer
    private fun isNearby(currentUserLocation: LatLng, otherUserLocation: LatLng): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            currentUserLocation.latitude, currentUserLocation.longitude,
            otherUserLocation.latitude, otherUserLocation.longitude,
            results
        )
        return results[0] < 1000 // Within 1 kilometer
    }

    override fun onDestroy() {
        super.onDestroy()
        userLocationListener?.remove() // Remove the Firestore listener to prevent memory leaks
    }
}



