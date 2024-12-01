package com.example.matchmakers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.matchmakers.network.RetrofitInstance
import com.example.matchmakers.databinding.ActivityMainBinding
import com.example.matchmakers.maplogic.LocationPermissionHelper
import com.example.matchmakers.maplogic.LocationService
import com.example.matchmakers.ui.auth.LoginActivity
import com.example.matchmakers.worker.UserSyncWorker
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class  MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manually trigger the worker for testing
        triggerUserSyncWorker(this)

        // Check if the user is logged in
        if (auth.currentUser == null) {
            // Redirect to LoginActivity if not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        println("Current Logged-In User ID: $userId")

        // Check and request location permission if not granted
        if(!LocationPermissionHelper.isLocationPermissionGranted(this)) {
            LocationPermissionHelper.requestLocationPermission(this)

        } else {
            LocationService(this).getLocation{ location ->
                if (location != null) {
                    // Handle the location here
                    val latitude = location.latitude
                    val longitude = location.longitude
                    println("Location: $latitude, $longitude")
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // update clusters (not everytime)
        updateClusters()

        val navView: BottomNavigationView = binding.navView

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_messages, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun updateClusters() {
        RetrofitInstance.api.updateClusters().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Clusters updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to update clusters", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // DK manually trigger the worker for update cache
    fun triggerUserSyncWorker(context: Context) {
        // Build the OneTimeWorkRequest
        val workRequest = OneTimeWorkRequest.Builder(UserSyncWorker::class.java)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}