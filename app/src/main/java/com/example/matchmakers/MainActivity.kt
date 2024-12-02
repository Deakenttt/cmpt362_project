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
import com.example.matchmakers.network.ClusterApiService
import com.example.matchmakers.ui.auth.LoginActivity
import com.example.matchmakers.worker.UserSyncWorker
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class  MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var apiService: ClusterApiService

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

        // Initialize the Retrofit service
        val retrofit = Retrofit.Builder()
            .baseUrl("https://recommand-sys-408256072995.us-central1.run.app") // Use your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ClusterApiService::class.java)

        // Call the POST method
        sendPostRequest()

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

    private fun sendPostRequest() {
        println("mgs: Sending POST request...")
        apiService.updateClusters().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("mgs: POST successful")
                } else {
                    println("mgs: POST failed with code: ${response.code()} - ${response.message()} - ${response.body()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("mgs: POST request failed due to: ${t.message}")
            }
        })
    }
}