package com.example.matchmakers.ui.createaccount

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.matchmakers.MainActivity
import com.example.matchmakers.R
import com.example.matchmakers.network.ClusterApiService
import com.example.matchmakers.ui.home.HomeFragment
import com.example.matchmakers.ui.profile.ImageUtils
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class CreateAccount : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var interest1Spinner: Spinner
    private lateinit var interest2Spinner: Spinner
    private lateinit var interest3Spinner: Spinner
    private lateinit var biographyEditText: TextInputEditText
    private lateinit var avatarImageView: ImageView
    private lateinit var galleryImageView1: ImageView
    private lateinit var galleryImageView2: ImageView
    private lateinit var galleryImageView3: ImageView
    private lateinit var avatarButton: Button
    private lateinit var addGalleryPhoto1Button: Button
    private lateinit var addGalleryPhoto2Button: Button
    private lateinit var addGalleryPhoto3Button: Button
    private lateinit var submitButton: Button
    private lateinit var genderGroup: RadioGroup
    private val viewModel: CreateAccountViewModel by viewModels()

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var tempImgUri: Uri

    private lateinit var apiService: ClusterApiService

    private var imageViewTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        ImageUtils.checkPermissions(this)

        // Initialize views
        nameEditText = findViewById(R.id.name)
        ageEditText = findViewById(R.id.age)
        genderGroup = findViewById(R.id.gender_group)
        interest1Spinner = findViewById(R.id.interest1_spinner)
        interest2Spinner = findViewById(R.id.interest2_spinner)
        interest3Spinner = findViewById(R.id.interest3_spinner)
        biographyEditText = findViewById<TextInputEditText>(R.id.create_bio)
        avatarImageView = findViewById(R.id.avatar_image)
        galleryImageView1 = findViewById(R.id.create_gallery_image_1)
        galleryImageView2 = findViewById(R.id.create_gallery_image_2)
        galleryImageView3 = findViewById(R.id.create_gallery_image_3)
        avatarButton = findViewById(R.id.add_avatar_button)
        addGalleryPhoto1Button = findViewById(R.id.add_gallery_photo_1_button)
        addGalleryPhoto2Button = findViewById(R.id.add_gallery_photo2_button)
        addGalleryPhoto3Button = findViewById(R.id.add_gallery_photo3_button)
        submitButton = findViewById(R.id.submit_button)

        val interestItems = listOf(
            "Select Interest",
            "Travel",
            "Football",
            "Snowboarding",
            "Swimming",
            "Drinking",
            "Poker",
            "Photography",
            "Movies",
            "Skiing",
            "Reading",
            "Hiking")

        interest1Spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            interestItems
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        interest2Spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            interestItems
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        interest3Spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            interestItems
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Set up camera and gallery launchers'
        val tempImgFile = File(getExternalFilesDir(null), "temp_gallery_image.jpg")
        if (tempImgFile.exists()) {
            println("mgs: deleting old temp image")
            tempImgFile.delete()
        }
        tempImgUri = FileProvider.getUriForFile(this, "com.example.matchmakers", tempImgFile)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                println("mgs: $tempImgUri")
                setImageForImageView(imageViewTag, tempImgUri)
            }
            else {
                println("mgs: Not working camera")
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                setImageForImageView(imageViewTag, it)
            }
        }

        observeImages()
        // Initialize the Retrofit service
        val retrofit = Retrofit.Builder()
            .baseUrl("https://recommand-sys-408256072995.us-central1.run.app") // Use your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ClusterApiService::class.java)



        // Submit button click listener
        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString().toIntOrNull()
            val biography = biographyEditText.text.toString()
            val interest1 = interest1Spinner.selectedItem.toString()
            val interest2 = interest2Spinner.selectedItem.toString()
            val interest3 = interest3Spinner.selectedItem.toString()

            // Check if name is valid
            if (name.isBlank()) {
                nameEditText.error = "Name cannot be empty."
                nameEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if age is valid
            if (age == null || age <= 0) {
                ageEditText.error = "Please enter a valid age."
                ageEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if biography is valid
            if (biography.isBlank()) {
                biographyEditText.error = "Biography cannot be empty."
                biographyEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if all interests are selected
            if (interest1 == "Select Interest" || interest2 == "Select Interest" || interest3 == "Select Interest") {
                Toast.makeText(this, "Please select all three interests.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check that all interests are unique
            if (interest1 == interest2 || interest2 == interest3 || interest1 == interest3){
                Toast.makeText(this, "You cannot select the same interest more than once.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if avatar image is set
            if (viewModel.avatarImage.value == null) {
                Toast.makeText(this, "Please set an avatar image.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if gallery images are set
            if (viewModel.galleryImage1.value == null ||
                viewModel.galleryImage2.value == null ||
                viewModel.galleryImage3.value == null) {
                Toast.makeText(this, "Please add all three gallery images.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGender = when (genderGroup.checkedRadioButtonId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                else -> null
            }

            // Check if gender is selected
            if (selectedGender == null) {
                Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Everything is valid, proceed to create account
            println("mgs: $name $age $interest1 $interest2 $interest3 $selectedGender")
            viewModel.createAccount(name, age, interest1, interest2, interest3, biography, selectedGender, this)
        }


        viewModel.accountCreated.observe(this, Observer { isCreated ->
            if (isCreated) {
                sendPostRequest()
            } else {
                // Implement more robust error handling later
                println("mgs: Error adding user info or user not logged in")
            }

        })

        // Set image click listeners
        // Set OnClickListeners for buttons
        avatarButton.setOnClickListener { clearFocus(); showImageSourceDialog("avatar_image") }
        addGalleryPhoto1Button.setOnClickListener { clearFocus(); showImageSourceDialog("create_gallery_image_1") }
        addGalleryPhoto2Button.setOnClickListener { clearFocus(); showImageSourceDialog("create_gallery_image_2") }
        addGalleryPhoto3Button.setOnClickListener { clearFocus(); showImageSourceDialog("create_gallery_image_3") }
    }

    private fun clearFocus(){
        nameEditText.clearFocus()
        ageEditText.clearFocus()
        biographyEditText.clearFocus()
    }

    private fun observeImages() {
        // Observe the avatar image in the ViewModel
        viewModel.avatarImage.observe(this, Observer { bitmap ->
            bitmap?.let {
                avatarImageView.setImageBitmap(it)
            }
        })

        // Observe the gallery images in the ViewModel
        viewModel.galleryImage1.observe(this, Observer { bitmap ->
            bitmap?.let {
                galleryImageView1.setImageBitmap(it)
            }
        })

        viewModel.galleryImage2.observe(this, Observer { bitmap ->
            bitmap?.let {
                galleryImageView2.setImageBitmap(it)
            }
        })

        viewModel.galleryImage3.observe(this, Observer { bitmap ->
            bitmap?.let {
                galleryImageView3.setImageBitmap(it)
            }
        })
    }

    private fun showImageSourceDialog(imageViewTag: String) {
        this.imageViewTag = imageViewTag
        val options = arrayOf("Take Photo", "Choose from Gallery")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

//    private fun setImageForImageView(tag: String, uri: Uri?) {
//        uri?.let {
//            // Load the bitmap from the URI
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
//
//            // Correct the orientation synchronously
//            val rotatedBitmap = viewModel.correctImageRotation(this, bitmap, uri)
//
//            when (tag) {
//                "avatar_image" -> {
//                    avatarImageView.setImageBitmap(bitmap)
////                    viewModel.updateAvatarImage(bitmap)
//                    viewModel.updateAvatarImage(rotatedBitmap)
//                }
//                "create_gallery_image_1" -> {
//                    galleryImageView1.setImageBitmap(bitmap)
////                    viewModel.updateGalleryImage1(bitmap)
//                    viewModel.updateGalleryImage1(this, bitmap, uri)
//                }
//                "create_gallery_image_2" -> {
//                    galleryImageView2.setImageBitmap(bitmap)
////                    viewModel.updateGalleryImage2(bitmap)
//                    viewModel.updateGalleryImage2(this, bitmap, uri)
//                }
//                "create_gallery_image_3" -> {
//                    galleryImageView3.setImageBitmap(bitmap)
////                    viewModel.updateGalleryImage3(bitmap)
//                    viewModel.updateGalleryImage3(this, bitmap, uri)
//                }
//            }
//        }
//    }

//  DK comment (update images to DB)
    private fun setImageForImageView(tag: String, uri: Uri?) {
        uri?.let {
            when (tag) {
                "avatar_image" -> {
                    viewModel.updateAvatarImage(this, it, targetWidth = 800, targetHeight = 800)
                }
                "create_gallery_image_1" -> {
                    viewModel.updateGalleryImage1(this, it, targetWidth = 800, targetHeight = 800)
                }
                "create_gallery_image_2" -> {
                    viewModel.updateGalleryImage2(this, it, targetWidth = 800, targetHeight = 800)
                }
                "create_gallery_image_3" -> {
                    viewModel.updateGalleryImage3(this, it, targetWidth = 800, targetHeight = 800)
                }
            }
        }
    }

    private fun sendPostRequest() {
        println("mgs: Sending POST request...")
        apiService.updateClusters().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("mgs: POST successful")
                    startActivity(Intent(this@CreateAccount, MainActivity::class.java))
                    finish()
                } else {
                    println("mgs: POST failed with code: ${response.code()} - ${response.message()} - ${response.body()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("mgs: POST request failed due to: ${t.message}")
            }
        })
    }

}
