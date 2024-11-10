package com.example.matchmakers.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentHomeBinding
import com.example.matchmakers.model.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupObservers()
        setupButtons()
        return binding.root
    }

    private fun setupObservers() {
        // Observe the current user and update UI
        homeViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                displayUserOnCards(user)
            } else {
                showNoMoreUsersMessage()
            }
        })
    }

    private fun setupButtons() {
        // Set up like button and dislike button for showing next user
        binding.likeButton1.setOnClickListener { homeViewModel.likeUser() }
        binding.dislikeButton1.setOnClickListener { homeViewModel.showNextUser() }
    }

    // Display the user's main info on the first card, and descriptions on other cards
    private fun displayUserOnCards(user: User) {
        // Card 1: Show user's name and age
        binding.userName1.text = user.name
        binding.userAge1.text = "Age: ${user.age}"
        binding.userInterest1.text = "Interests: ${user.interest}"
        binding.userImage1.setImageResource(R.drawable.sample_user_image) // Placeholder image

        // Card 2: Show a description of hobbies or general bio
        binding.userInterest2.text = "Loves spending time outdoors and exploring new hobbies."
        binding.userImage2.setImageResource(R.drawable.sample_user_image) // Placeholder image

//        // Card 3: Show a different description or aspect of the user
//        binding.userInterest3.text = "${user.name} is passionate about ${user.interest} and enjoys social gatherings."
//        binding.userImage3.setImageResource(R.drawable.sample_user_image) // Placeholder image
//
//        // Card 4: Another unique description
//        binding.userInterest4.text = "Enjoys participating in community events and has a knack for storytelling."
//        binding.userImage4.setImageResource(R.drawable.sample_user_image) // Placeholder image
//
//        // Card 5: Additional interests or details about the user
//        binding.userInterest5.text = "${user.name} is always eager to learn and meet new people."
//        binding.userImage5.setImageResource(R.drawable.sample_user_image) // Placeholder image
//
//        // Card 6: Show the user's favorite activities
//        binding.userInterest6.text = "Favorite activities include hiking, cooking, and watching movies."
//        binding.userImage6.setImageResource(R.drawable.sample_user_image) // Placeholder image
    }

    // Show a message when there are no more users
    private fun showNoMoreUsersMessage() {
        binding.userName1.text = "No more users to display"
        binding.userAge1.text = ""
        binding.userInterest1.text = ""
        binding.userImage1.setImageResource(R.drawable.sample_user_image) // Keep placeholder image
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
