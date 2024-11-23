package com.example.matchmakers.ui.messages

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.matchmakers.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatListenerService : Service() {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate() {
        super.onCreate()
        startListeningForMessages()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListeningForMessages()
    }

    private fun startListeningForMessages() {
        val conversationId = ""
        val conversationRef = db.collection("conversations").document(conversationId)

        listenerRegistration = conversationRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                println("Error fetching messages: $exception")
                return@addSnapshotListener
            }

            documentSnapshot?.let {
                val messages = it.get("messages") as? List<Map<String, String>> ?: emptyList()
                showNotification("New message", "You have a new message!")
            }
        }
    }

    private fun stopListeningForMessages() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "message_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
