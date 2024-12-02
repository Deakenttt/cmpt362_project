package com.example.matchmakers.ui.messages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentMessagesBinding
import com.example.matchmakers.model.User
import com.google.firebase.auth.FirebaseAuth

class MessagesFragment: Fragment() {
    private var usersArray = listOf<User>()
    private lateinit var messagesList: ListView
    private lateinit var adapter: MessagesAdapter
    private lateinit var receiverId: String
    private val auth = FirebaseAuth.getInstance()

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val messagesViewModel = ViewModelProvider(this)[MessagesViewModel::class.java]

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val context = requireActivity()

        messagesViewModel.fetchMatches()

        // Observe changes to the users list and update the adapter
        messagesViewModel.usersList.observe(viewLifecycleOwner, Observer { userList ->
            usersArray = userList
            // Update the adapter with the new list
            adapter.updateEntries(usersArray)
        })

        messagesList = root.findViewById(R.id.messages_list)
        // Initialize the adapter
        adapter = MessagesAdapter(context, usersArray)
        messagesList.adapter = adapter

        val currentUserId = auth.currentUser?.uid
        messagesList.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            println(usersArray[position].name)

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ChatActivity.USER_ID_KEY, usersArray[position].id)
            intent.putExtra(ChatActivity.USER_NAME_KEY, usersArray[position].name)
            intent.putExtra(ChatActivity.USER_AGE_KEY, usersArray[position].age)
            intent.putExtra(ChatActivity.CURRENT_USER_ID_KEY, currentUserId)
            intent.putExtra(ChatActivity.CONVERSATION_ID_KEY, usersArray[position].conversationId)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
