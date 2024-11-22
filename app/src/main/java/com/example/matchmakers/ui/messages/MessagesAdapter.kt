package com.example.matchmakers.ui.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.matchmakers.R
import com.example.matchmakers.model.User

class MessagesAdapter(private val context: Context, private var entries: List<User>): BaseAdapter() {
    override fun getItem(position: Int): Any {
        return entries[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return entries.size
    }

    fun updateEntries(newEntries: List<User>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.messages_list_item, null)

        val username = view.findViewById<TextView>(R.id.messages_username)
        val age = view.findViewById<TextView>(R.id.messages_age)
        val lastMessage = view.findViewById<TextView>(R.id.messages_lastMessage)

        val data = entries[position]

        username.text = data.name
        age.text = data.age.toString()
        lastMessage.text = data.lastMessage

        return view
    }
}