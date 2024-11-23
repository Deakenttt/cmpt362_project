package com.example.matchmakers.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentHomeBinding
import com.example.matchmakers.repository.UserRepository
import com.example.matchmakers.viewmodel.UserViewModel
import com.example.matchmakers.viewmodel.UserViewModelFactory
import com.example.matchmakers.model.User
import com.google.android.material.snackbar.Snackbar
import com.example.matchmakers.database.UserDatabase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        initialize()
        return binding.root
    }

    /**
     * Initialize ViewModels, Observers, and Buttons
     */
    private fun initialize() {
        setupViewModels()
        setupObservers()
        setupButtons()
    }

    private fun setupViewModels() {
        // Initialize the UserDatabase singleton instance
        val userDatabase = UserDatabase.getDatabase(requireContext())

        // Obtain the UserDao from the database
        val userDao = userDatabase.userDao()

        // Initialize UserRepository with UserDao
        val userRepository = UserRepository(userDao)

        // Initialize UserViewModel with UserViewModelFactory
        val userViewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]

        // Initialize HomeViewModel with HomeViewModelFactory
        val homeViewModelFactory = HomeViewModelFactory(userViewModel)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
    }

    private fun setupObservers() {
        // Observe the current recommended user and update the UI
        homeViewModel.currentRecommendedUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                displayUserOnCards(user)
            } else {
                showNoMoreUsersMessage()
            }
        }

        // Observe error messages and show them using a Snackbar
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButtons() {
        // Handle like and dislike button clicks
        binding.likeButton1.setOnClickListener { homeViewModel.likeUser() }
        binding.dislikeButton1.setOnClickListener { homeViewModel.showNextUser() }
    }

    /**
     * Dynamically display the recommended user's details and images on all 6 cards.
     */
    private fun displayUserOnCards(user: User) {
        // Example: Assuming the User model has a `pictures` field containing 6 image resource IDs
//        val pictures = user.pictures // List of image resource IDs (Int)

        // Card 1: Show basic info with the first picture
        binding.username.text = user.name
        binding.userAge.text = "Age: ${user.age}"
        binding.userInterest1.text = "Interests: ${user.interest}"
//        binding.userImage1.setImageResource(pictures[0]) // Show first picture

        // Card 2: Show the second picture and additional description
        binding.userInterest2.text = "Explores new hobbies and loves adventure."
//        binding.userImage2.setImageResource(pictures[1])

        // Card 3: Show the third picture with another description
//        binding.userInterest3.text = "${user.name} is passionate about ${user.interest}."
//        binding.userImage3.setImageResource(pictures[2])
//
//        // Card 4: Show the fourth picture and personalized detail
//        binding.userInterest4.text = "Enjoys connecting with like-minded individuals."
//        binding.userImage4.setImageResource(pictures[3])
//
//        // Card 5: Show the fifth picture and hobbies
//        binding.userInterest5.text = "${user.name} spends time on photography and social events."
//        binding.userImage5.setImageResource(pictures[4])
//
//        // Card 6: Show the sixth picture and closing details
//        binding.userInterest6.text = "Enjoys hiking, cooking, and exploring new cultures."
//        binding.userImage6.setImageResource(pictures[5])
    }

    /**
     * Show a message when no recommended users are available.
     */
    private fun showNoMoreUsersMessage() {
        // Card 1: Display "no more users" message
        binding.username.text = "No more users available"
        binding.userAge.text = ""
        binding.userInterest1.text = ""
//        binding.userImage1.setImageResource(R.drawable.sample_user_image) // Placeholder image

        // Hide content in other cards
        binding.userInterest2.text = ""
//        binding.userImage2.setImageResource(R.drawable.sample_user_image)

//        binding.userInterest3.text = ""
//        binding.userImage3.setImageResource(R.drawable.sample_user_image)
//
//        binding.userInterest4.text = ""
//        binding.userImage4.setImageResource(R.drawable.sample_user_image)
//
//        binding.userInterest5.text = ""
//        binding.userImage5.setImageResource(R.drawable.sample_user_image)
//
//        binding.userInterest6.text = ""
//        binding.userImage6.setImageResource(R.drawable.sample_user_image)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
