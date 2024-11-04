package com.example.matchmakers.ui.messages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.matchmakers.R

class ChatActivity: AppCompatActivity() {
    private var userId = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intentUserId = intent.getStringExtra(USER_ID_KEY)
        if (intentUserId != null){
            userId = intentUserId
        }
        val intentUserName = intent.getStringExtra(USER_NAME_KEY)
        if (intentUserName != null){
            userName = intentUserName
        }
    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
    }
}