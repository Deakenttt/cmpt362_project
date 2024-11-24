package com.example.matchmakers.ui.report

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.matchmakers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportActivity: AppCompatActivity() {
    private var userId = ""
    private var userName = ""

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var reportTitleText: TextView
    private lateinit var reportReason: RadioGroup
    private lateinit var reportInput: EditText
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
        reportReason = findViewById(R.id.report_reason_input)
        reportInput = findViewById(R.id.report_other_text)
        submitButton = findViewById(R.id.submit_button)

        val titleTextString = "Select a reason for reporting $userName"
        reportTitleText.text = titleTextString

        submitButton.setOnClickListener{
            val user = auth.currentUser ?: return@setOnClickListener

            val reportTypeId = reportReason.checkedRadioButtonId
            val reportDesc = reportInput.text.toString()

            if (reportTypeId == -1) {
                Toast.makeText(this, "Select a report reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reportType = findViewById<RadioButton>(reportTypeId).text.toString()

            val report = hashMapOf(
                "user" to userId,
                "reportedBy" to user.uid,
                "reason" to reportType,
                "description" to reportDesc,
                "time" to System.currentTimeMillis()
            )

            db.collection("reports").add(report).addOnSuccessListener {
                Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{error ->
                println(error)
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
    }
}