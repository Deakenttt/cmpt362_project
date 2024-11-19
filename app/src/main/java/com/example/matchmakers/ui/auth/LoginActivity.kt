package com.example.matchmakers.ui.auth

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.matchmakers.MainActivity
import com.example.matchmakers.R
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerRedirectButton: Button
    private var REQUEST_LOCATION_PERMISSION = 100

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var sharedPref: SharedPreferences
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerRedirectButton = findViewById(R.id.registerRedirectButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            Toast.makeText(this, "MatchMakers needs location permissions", Toast.LENGTH_SHORT).show()
        }

        // Observe login result
        loginViewModel.loginResult.observe(this) { success ->
            if (success) {
                // If the login was successful, save the login credentials so the user doesn't have to enter them again
                // They can log out from the profile fragment
                storeLogin(email, password)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Observe error messages
        loginViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            email = emailEditText.text.toString()
            password = passwordEditText.text.toString()
            loginViewModel.login(email, password)
        }

        registerRedirectButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // When the app starts, check for a saved login and if it exists, automatically login with it
        loadLogin()
    }

    private fun storeLogin(email: String, password: String){
        if (email == "" || password == ""){
            return
        }

        with (sharedPref.edit()){
            clear()
            putString(EMAIL_KEY, email)
            putString(PASSWORD_KEY, password)
            apply()
        }
    }

    private fun loadLogin(){
        if (!(sharedPref.contains(EMAIL_KEY) && sharedPref.contains(PASSWORD_KEY))){
            return
        }

        val email = sharedPref.getString(EMAIL_KEY, "")
        val password = sharedPref.getString(PASSWORD_KEY, "")

        if (email != null && password != null){
            loginViewModel.login(email, password)
        }
    }

    companion object{
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
    }
}
