package com.example.matchmakers.ui.messages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.matchmakers.R
import com.example.matchmakers.model.ChatMessage
import com.example.matchmakers.ui.profile.OtherProfileActivity

class ChatActivity: AppCompatActivity() {
    private var currentUserId = ""
    private var conversationId = ""
    private var userId = ""
    private var userName = ""

    private var messagesArray = ArrayList<ChatMessage>()
    private lateinit var chatList: ListView
    private lateinit var adapter: ChatAdapter

    private lateinit var usernameText: TextView
    private lateinit var viewProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intentCurrentUser = intent.getStringExtra(CURRENT_USER_ID_KEY)
        val intentConversationId = intent.getStringExtra(CONVERSATION_ID_KEY)
        val intentUserId = intent.getStringExtra(USER_ID_KEY)
        val intentUserName = intent.getStringExtra(USER_NAME_KEY)
        if (intentCurrentUser != null){
            currentUserId = intentCurrentUser
        }
        if (intentConversationId != null){
            conversationId = intentConversationId
        }
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

        usernameText = findViewById(R.id.chat_username)
        usernameText.text = userName

        viewProfileButton = findViewById(R.id.chat_view_profile)
        viewProfileButton.setOnClickListener{
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra(OtherProfileActivity.USER_ID_KEY, userId)
            intent.putExtra(OtherProfileActivity.USER_NAME_KEY, userName)
            startActivity(intent)
        }

        // Change this later to get the actual chat with the user from the database
        messagesArray = arrayListOf(
            ChatMessage(fromUserId="1", message="This"),
            ChatMessage(fromUserId="1", message="is"),
            ChatMessage(fromUserId="1", message="an"),
            ChatMessage(fromUserId="1", message="example"),
            ChatMessage(fromUserId="1", message="chat"),
            ChatMessage(fromUserId="0", message="It will be replaced with actual data from the database later"),
            ChatMessage(fromUserId="1", message="This"),
            ChatMessage(fromUserId="1", message="is"),
            ChatMessage(fromUserId="1", message="here"),
            ChatMessage(fromUserId="0", message="to make sure the UI is working and it can display long messages 9h oi wuibiunnj miklnijop pji a nm, xc knd jkm; klfl ;kmrt jrklm t; jierre xz 89u0 hx  nkl  mlk;r6f qwmpik ljnk u8 t9ihu4 "),
            ChatMessage(fromUserId="1", message="Later, this data class can support other types of messages such as images")
        )
        chatList = findViewById(R.id.chat_list)
        adapter = ChatAdapter(this, messagesArray, currentUserId)
        chatList.adapter = adapter
    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
        const val CURRENT_USER_ID_KEY = "current_id"
        const val CONVERSATION_ID_KEY = "conversation_id"
    }
}