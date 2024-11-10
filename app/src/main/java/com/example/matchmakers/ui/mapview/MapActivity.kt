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
import com.google.firebase.firestore.FirebaseFirestore

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var locationService: LocationService
    private val mapDataHandler = MapDataHandler()
    private val firestore = FirebaseFirestore.getInstance() // Firebase Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        locationService = LocationService(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val backButton = findViewById<Button>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Show user's current location
        locationService.getLastKnownLocation { location ->
            if (location != null) {
                val userLocation = LatLng(location.latitude, location.longitude)
                mapDataHandler.setCameraToUserLocation(googleMap, userLocation)
                googleMap.addMarker(mapDataHandler.createMarkerOptions(userLocation, "You are here"))
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }

        // Loading another user's location from Firebase
        loadOtherUsersFromFirebase()
    }

    private fun loadOtherUsersFromFirebase() {
        firestore.collection("users")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users = task.result?.documents?.mapNotNull { document ->
                        val name = document.getString("name") ?: "Unknown"
                        val interest = document.getString("interest") ?: "None"
                        val gender = document.getString("gender") ?: "Unknown"
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")

                        if (latitude != null && longitude != null) {
                            UserInfo(name, interest, gender, latitude, longitude)
                        } else null
                    } ?: emptyList()

                    // Adding user markers to the map
                    mapDataHandler.addUsersMarkers(googleMap, users)
                } else {
                    Toast.makeText(this, "Failed to load user locations", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


