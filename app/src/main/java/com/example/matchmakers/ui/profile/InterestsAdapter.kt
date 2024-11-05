package com.example.matchmakers.ui.profile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.matchmakers.R

class InterestsAdapter(private val context: Context, private var entries: List<String>): BaseAdapter() {
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
        val view = View.inflate(context, R.layout.interests_list_item, null)

        val name = view.findViewById<TextView>(R.id.interest_name)
        val image = view.findViewById<ImageView>(R.id.interest_image)

        val data = entries[position]
        name.text = data

        return view
    }
}