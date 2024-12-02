package com.example.matchmakers.ui.messages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.matchmakers.R
import com.example.matchmakers.model.ChatMessage
import com.example.matchmakers.ui.profile.OtherProfileActivity

class ChatActivity: AppCompatActivity() {
    private var currentUserId = ""
    private var conversationId = ""
    private var userId = ""
    private var userName = ""
    private var receiverId = ""

    private lateinit var sendButton: Button
    private lateinit var textToSend: EditText

    private var messagesArray = listOf<ChatMessage>()
    private lateinit var chatList: ListView
    private lateinit var adapter: ChatAdapter

    private lateinit var usernameText: TextView
    private lateinit var ageText: TextView
    private lateinit var viewProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intentCurrentUser = intent.getStringExtra(CURRENT_USER_ID_KEY)
        val intentConversationId = intent.getStringExtra(CONVERSATION_ID_KEY)
        val intentUserId = intent.getStringExtra(USER_ID_KEY)
        val intentUserName = intent.getStringExtra(USER_NAME_KEY)
        val intentUserAge = intent.getIntExtra(USER_AGE_KEY, 0)
        intent.getStringExtra(RECEIVER_ID_KEY)
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

        ageText = findViewById(R.id.chat_age)
        ageText.text = intentUserAge.toString()


        viewProfileButton = findViewById(R.id.chat_view_profile)
        viewProfileButton.setOnClickListener{
            val intent = Intent(this, OtherProfileActivity::class.java)
            intent.putExtra(OtherProfileActivity.USER_ID_KEY, userId)
            intent.putExtra(OtherProfileActivity.USER_NAME_KEY, userName)
            startActivity(intent)
        }

        chatList = findViewById(R.id.chat_list)
        adapter = ChatAdapter(this, messagesArray, currentUserId)
        chatList.adapter = adapter

        chatList.setOnItemClickListener{_,_,_,_->textToSend.clearFocus()}

        sendButton = findViewById(R.id.chat_send)
        textToSend = findViewById(R.id.chat_input)

        val chatViewModel: ChatViewModel by viewModels()
        chatViewModel.fetchMessages(conversationId)
        chatViewModel.messages.observe(this, Observer { messages ->
            messagesArray = messages
            println("mgs: messages: $messages")
            adapter.updateEntries(messagesArray)

            chatList.post{
                // Scroll to the bottom each time a new message is posted
                chatList.scrollListBy(2147483647)
            }
        })

        val timestamp = chatViewModel.getCurrentTimestamp()

        sendButton.setOnClickListener{
            val msgToSend = textToSend.text.toString()
            val messageObject = chatViewModel.stringToChatMessage(
                "",
                "text",
                userId,
                currentUserId,
                msgToSend,
                timestamp
                )

            chatViewModel.sendMessage(messageObject, conversationId)
            textToSend.setText("")
            // Refresh if needed unless listeners take care of this
        }

    }

    companion object{
        const val USER_ID_KEY = "id"
        const val USER_NAME_KEY = "username"
        const val USER_AGE_KEY = "age"
        const val CURRENT_USER_ID_KEY = "current_id"
        const val CONVERSATION_ID_KEY = "conversation_id"
        const val RECEIVER_ID_KEY = "receiver_id"
    }
}