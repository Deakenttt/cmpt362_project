package com.example.matchmakers.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentMessagesBinding
import com.example.matchmakers.model.User
import com.example.matchmakers.ui.notifications.NotificationsViewModel

class MessagesFragment: Fragment() {
    private var usersArray = ArrayList<User>()
    private lateinit var messagesList: ListView
    private lateinit var adapter: MessagesAdapter

    private var _binding: FragmentMessagesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val messagesViewModel =
            ViewModelProvider(this)[MessagesViewModel::class.java]

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireActivity()

        // This is fake data, only used to make sure the UI elements are working. Once the database is set up
        // with all necessary information, this array will be filled using the database instead.
        usersArray = arrayListOf(
            User(id="0", name="Bull", age=148, interest="interest 1"),
            User(id="1", name="Frank", age=99, interest="interest 2"),
            User(id="2", name="User 1", age=-4, interest="interest 3"),
            User(id="3", name="User 2", age=0, interest="interest 4"),
            User(id="4", name="David", age=2115, interest="interest 5")
        )
        messagesList = root.findViewById(R.id.messages_list)
        adapter = MessagesAdapter(context, usersArray)
        messagesList.adapter = adapter

        messagesList.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            println(usersArray[position].name)
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ChatActivity.USER_ID_KEY, usersArray[position].id)
            intent.putExtra(ChatActivity.USER_NAME_KEY, usersArray[position].name)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}