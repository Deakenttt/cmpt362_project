package com.example.matchmakers.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.ui.report.ReportActivity

class OtherProfileActivity: AppCompatActivity() {
    private var userId = ""
    private var userName = ""

    private var interestsArray = ArrayList<String>()
    private lateinit var interestsList: GridView
    private lateinit var adapter: InterestsAdapter

    private lateinit var usernameText: TextView
    private lateinit var ageText: TextView
    private lateinit var biographyText: TextView
    private lateinit var blockButton: Button
    private lateinit var reportButton: Button
    private lateinit var avatarImage: ImageView

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

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

        interestsArray = arrayListOf()
        interestsList = findViewById(R.id.profile_interests)
        adapter = InterestsAdapter(this, interestsArray)
        interestsList.adapter = adapter

        usernameText = findViewById(R.id.text_username)
        ageText = findViewById(R.id.text_age)
        biographyText = findViewById(R.id.text_biography)
        blockButton = findViewById(R.id.block_button)
        reportButton = findViewById(R.id.report_button)
        avatarImage = findViewById(R.id.other_avatar_image)

        profileViewModel.updateOtherAvatar(this, userId)
        profileViewModel.loadOtherProfile(userId)
        profileViewModel.otherAvatar.observe(this){
            bitmap -> avatarImage.setImageBitmap(bitmap)
        }
        profileViewModel.otherProfile.observe(this){
            usernameText.text = it.name
            ageText.text = it.age.toString()
            biographyText.text = it.biography

            interestsArray.clear()
            interestsArray.addAll(it.interests)
            adapter.notifyDataSetChanged()
        }

        reportButton.setOnClickListener{
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra(ReportActivity.USER_ID_KEY, userId)
            intent.putExtra(ReportActivity.USER_NAME_KEY, userName)
            startActivity(intent)
        }
    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
    }
}