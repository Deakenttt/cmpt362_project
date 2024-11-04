package com.example.matchmakers.ui.CreateAccount

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.matchmakers.MainActivity
import com.example.matchmakers.R
import com.google.android.material.textfield.TextInputEditText

class CreateAccount : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var interestEditText: TextInputEditText
    private lateinit var submitButton: Button
    private lateinit var genderGroup: RadioGroup
    private val viewModel: CreateAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        nameEditText = findViewById(R.id.name)
        ageEditText = findViewById(R.id.age)
        interestEditText = findViewById(R.id.interests)
        genderGroup = findViewById(R.id.gender_group)

        submitButton = findViewById(R.id.submit_button)

        viewModel.accountCreated.observe(this, Observer { isCreated ->
            if (isCreated) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Implement more robust error handling later
                println("mgs: Error adding user info or user not logged in")
            }
        })

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString().toInt()
            val interest = interestEditText.text.toString()
            val selectedGender = when(genderGroup.checkedRadioButtonId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                R.id.gender_other -> "Other"
                else -> "Not specified"
            }
            viewModel.createAccount(name, age, interest, selectedGender)
        }
    }
}
