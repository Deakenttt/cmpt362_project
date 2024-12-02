package com.example.matchmakers.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.matchmakers.R
import com.example.matchmakers.ui.createaccount.CreateAccount
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var regButton: Button
    private lateinit var loginRedirectButton: Button

    private lateinit var sharedPref: SharedPreferences

    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        regButton = findViewById(R.id.registerButton)
        loginRedirectButton = findViewById(R.id.loginRedirectButton)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Observe registration result
        registerViewModel.registrationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CreateAccount::class.java))
                finish()
            }
        }

        // Store the login information if the registration succeeds
        registerViewModel.storedLogin.observe(this) { login ->
            val email = login.email
            val password = login.password
            if (email != "" && password != ""){
                with (sharedPref.edit()){
                    clear()
                    putString(LoginActivity.EMAIL_KEY, email)
                    putString(LoginActivity.PASSWORD_KEY, password)
                    apply()
                }
            }
        }

        // Observe error messages
        registerViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        regButton.setOnClickListener {
            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()
            registerViewModel.register(email, password)
        }

        loginRedirectButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
