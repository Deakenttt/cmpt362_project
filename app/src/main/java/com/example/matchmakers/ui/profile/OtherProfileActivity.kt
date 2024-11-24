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
        // userId will be used to get the current user's profile information from the database

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        interestsArray = arrayListOf("Interest 1", "Interest 2", "Interest 3", "Interest 4", "Interest 5")
        interestsList = findViewById(R.id.profile_interests)
        adapter = InterestsAdapter(this, interestsArray)
        interestsList.adapter = adapter

        usernameText = findViewById(R.id.text_username)
        ageText = findViewById(R.id.text_age)
        biographyText = findViewById(R.id.text_biography)
        blockButton = findViewById(R.id.block_button)
        reportButton = findViewById(R.id.report_button)
        avatarImage = findViewById(R.id.other_avatar_image)

        usernameText.text = userName
        ageText.text = "User's age"
        biographyText.text = "sld fslkd fdslkfd slfkds slk fslk dflksd jfsdlkf dsjlkfds jdflsk jfdslkf dslksdf sldfk fsdl fsdlkfsd fsldkskl fdsjlfdks fldsk fdslk sslf lfds dfsl f sldf sldk lks fjslfs fdlsfds jlkfds fjdslkds js dfjlf sdfjdslk fdslkjdfs fldsk fdjslkfds jflkds dslk dslkf dslksd fsldf sdlkfds flkds dslkdslkdsfsd lkf dsfljksd fdslkf dslkfsd flksd sdlkds lks g;sld ghs;lgg hsldfghl;sg df dflfd glfd g whr whp rojt tper tjerlktejterkt jer;ltrekjtre;lk trejl;terk jtrel;kre jl;k j;lkerjtel;rk tjer itoypeyo8rpwor hwyeiorp  hgiu hxp hxpvx njplk ngkl nfkjgnkjl bhiu go hgo8 5y3083045ni"

        profileViewModel.updateOtherAvatar(this, userId)
        profileViewModel.otherAvatar.observe(this){
            bitmap -> avatarImage.setImageBitmap(bitmap)
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