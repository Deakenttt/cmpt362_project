package com.example.matchmakers.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.databinding.FragmentHomeBinding
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.viewmodel.UserViewModel
import com.example.matchmakers.viewmodel.UserViewModelFactory
import com.example.matchmakers.model.User
import com.google.android.material.snackbar.Snackbar
import com.example.matchmakers.database.UserDatabase
import com.example.matchmakers.network.ClusterApiService
import com.example.matchmakers.repository.RemoteUserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel

    private var db = FirebaseFirestore.getInstance()
    private var storage = Firebase.storage
    private var ref = storage.reference



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
        val localUserRepository = LocalUserRepository(userDao)
        val remoteUserRepository = RemoteUserRepository()

        // Initialize UserViewModel with UserViewModelFactory
        val userViewModelFactory = UserViewModelFactory(localUserRepository, remoteUserRepository)
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

        homeViewModel.mutualLikeStatus.observe(viewLifecycleOwner)  { isMutual ->
            if(isMutual == true) {
                Toast.makeText(requireContext(), "Congrats, you matched! Messages inbox now open!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButtons() {
        // Handle like and dislike button clicks
        binding.likeButton1.setOnClickListener { homeViewModel.likeUser() }
        binding.dislikeButton1.setOnClickListener { homeViewModel.dislikeUser() }

    }

    /**
     * Dynamically display the recommended user's details and images on all 6 cards.
     */
    private fun displayUserOnCards(user: User) {
        // Example: Assuming the User model has a `pictures` field containing 6 image resource IDs
//        val pictures = user.pictures // List of image resource IDs (Int)

        val displayedUserUid = user.id
        db.collection("profileinfo").document(displayedUserUid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                binding.username.text = name
                val age = document.getLong("age")?.toInt()
                binding.userAge.text = age.toString()
                val biography = document.getString("biography")
                binding.userBio.text = biography
                val interest1 = document.getString("interest1")
                val interest2 = document.getString("interest2")
                val interest3 = document.getString("interest3")
                binding.userInterests.text = "${interest1}, ${interest2}, ${interest3}"
                val avatarRef = ref.child("avatars/${displayedUserUid}.jpg")
                loadBitmapIntoView(avatarRef, binding.userImage)
                val images = document.get("images") as? List<String>
                if (images != null && images.isNotEmpty()) {
                    val image1 = images.getOrNull(0)
                    val image2 = images.getOrNull(1)
                    val image3 = images.getOrNull(2)
                    val image1Ref = ref.child("gallery/${displayedUserUid}/${image1}")
                    val image2Ref = ref.child("gallery/${displayedUserUid}/${image2}")
                    val image3Ref = ref.child("gallery/${displayedUserUid}/${image3}")

                    loadBitmapIntoView(image1Ref, binding.userImage1)
                    loadBitmapIntoView(image2Ref, binding.userImage2)
                    loadBitmapIntoView(image3Ref, binding.userImage3)
                }
            }
    }

    private fun loadBitmapIntoView(imageRef: StorageReference, imageView: ImageView) {
        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Thread {
                    try {
                        // Open an InputStream to fetch the image data
                        val connection = URL(uri.toString()).openConnection()
                        val inputStream = connection.getInputStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)

                        // Set the bitmap to the ImageView on the main thread
                        imageView.post {
                            imageView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        println("Failed to load image: ${e.message}")
                    }
                }.start()
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch image URL: ${exception.message}")
            }
    }

    /**
     * Show a message when no recommended users are available.
     */
    private fun showNoMoreUsersMessage() {
        // Add some sort of popup to inform user that no more users are available currently
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}
