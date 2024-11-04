package com.example.matchmakers.ui.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.matchmakers.R
import com.example.matchmakers.model.ChatMessage

class ChatAdapter(private val context: Context, private var entries: List<ChatMessage>, private var userId: String): BaseAdapter() {
    override fun getItem(position: Int): Any {
        return entries[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return entries.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = View.inflate(context, R.layout.chat_list_item, null)

        val text = view.findViewById<TextView>(R.id.chat_text)

        val data = entries[position]

        text.text = data.message

        // If the user sending the message is the same as the user who is logged in, display the message aligned right
        if (data.fromUserId == userId){
            val params = text.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            text.layoutParams = params
        }

        return view
    }
}