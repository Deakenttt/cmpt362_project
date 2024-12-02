package com.example.matchmakers.ui.createaccount

import android.app.Activity
import android.content.Intent
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
import com.example.matchmakers.MainActivity
import com.example.matchmakers.R
import com.example.matchmakers.ui.profile.ImageUtils
import com.google.android.material.textfield.TextInputEditText
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
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Implement more robust error handling later
                println("mgs: Error adding user info or user not logged in")
            }
        })

        // Set image click listeners
        // Set OnClickListeners for buttons
        avatarButton.setOnClickListener { showImageSourceDialog("avatar_image") }
        addGalleryPhoto1Button.setOnClickListener { showImageSourceDialog("create_gallery_image_1") }
        addGalleryPhoto2Button.setOnClickListener { showImageSourceDialog("create_gallery_image_2") }
        addGalleryPhoto3Button.setOnClickListener { showImageSourceDialog("create_gallery_image_3") }
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

    private fun setImageForImageView(tag: String, uri: Uri?) {
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)

            when (tag) {
                "avatar_image" -> {
                    avatarImageView.setImageBitmap(bitmap)
                    viewModel.updateAvatarImage(bitmap)
                }
                "create_gallery_image_1" -> {
                    galleryImageView1.setImageBitmap(bitmap)
                    viewModel.updateGalleryImage1(bitmap)
                }
                "create_gallery_image_2" -> {
                    galleryImageView2.setImageBitmap(bitmap)
                    viewModel.updateGalleryImage2(bitmap)
                }
                "create_gallery_image_3" -> {
                    galleryImageView3.setImageBitmap(bitmap)
                    viewModel.updateGalleryImage3(bitmap)
                }
            }
        }
    }
}
