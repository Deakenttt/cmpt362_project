package com.example.matchmakers.ui.report

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.matchmakers.R

class ReportActivity: AppCompatActivity() {
    private var userId = ""
    private var userName = ""

    private lateinit var reportTitleText: TextView
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val intentUserId = intent.getStringExtra(USER_ID_KEY)
        val intentUserName = intent.getStringExtra(USER_NAME_KEY)
        if (intentUserId != null){
            userId = intentUserId
        }
        if (intentUserName != null){
            userName = intentUserName
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        reportTitleText = findViewById(R.id.report_title)
        submitButton = findViewById(R.id.submit_button)

        val titleTextString = "Select a reason for reporting $userName"
        reportTitleText.text = titleTextString

        submitButton.setOnClickListener{
            Toast.makeText(this, "Report submitted (doesn't actually do anything yet)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
    }
}