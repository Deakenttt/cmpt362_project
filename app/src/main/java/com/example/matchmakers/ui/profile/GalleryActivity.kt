package com.example.matchmakers.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import java.io.File

class GalleryActivity: AppCompatActivity() {
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var container1: LinearLayout
    private lateinit var image1: ImageView
    private lateinit var edit1: Button
    private lateinit var delete1: Button
    private lateinit var container2: LinearLayout
    private lateinit var image2: ImageView
    private lateinit var edit2: Button
    private lateinit var delete2: Button
    private lateinit var container3: LinearLayout
    private lateinit var image3: ImageView
    private lateinit var edit3: Button
    private lateinit var delete3: Button
    private lateinit var addButton: Button

    private lateinit var editDialog: EditAvatarDialog
    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<PickVisualMediaRequest>

    private var editingIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // User is now forced to have exactly 3 images so no list is necessary...
        container1 = findViewById(R.id.gallery_container_1)
        image1 = findViewById(R.id.gallery_image_1)
        edit1 = findViewById(R.id.gallery_edit_1)
        delete1 = findViewById(R.id.gallery_delete_1)
        container2 = findViewById(R.id.gallery_container_2)
        image2 = findViewById(R.id.gallery_image_2)
        edit2 = findViewById(R.id.gallery_edit_2)
        delete2 = findViewById(R.id.gallery_delete_2)
        container3 = findViewById(R.id.gallery_container_3)
        image3 = findViewById(R.id.gallery_image_3)
        edit3 = findViewById(R.id.gallery_edit_3)
        delete3 = findViewById(R.id.gallery_delete_3)
        addButton= findViewById(R.id.gallery_add_image)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        editDialog = EditAvatarDialog()
        editDialog.galleryActivity = this

        val tempImgFile = File(getExternalFilesDir(null), "temp_gallery_image.jpg")
        tempImgUri = FileProvider.getUriForFile(this, "com.example.matchmakers", tempImgFile)
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK){
                val rotation = ImageUtils.getCameraRotation(this)
                val bitmap = ImageUtils.getBitmap(this, tempImgUri, rotation)

                if (editingIndex < 0){
                    profileViewModel.addGalleryImage(this, bitmap)
                } else{
                    profileViewModel.replaceGalleryImage(this, bitmap, editingIndex)
                }
            }
        }
        galleryResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
            uri: Uri? ->
            if (uri != null){
                val bitmap = ImageUtils.getBitmap(this, uri, 90f)

                if (editingIndex < 0){
                    profileViewModel.addGalleryImage(this, bitmap)
                } else{
                    profileViewModel.replaceGalleryImage(this, bitmap, editingIndex)
                }
            }
        }

        edit1.setOnClickListener{
            editingIndex = 0
            openDialog()
        }
        edit2.setOnClickListener{
            editingIndex = 1
            openDialog()
        }
        edit3.setOnClickListener{
            editingIndex = 2
            openDialog()
        }
        delete1.setOnClickListener{
            profileViewModel.removeGalleryImage(this, 0)
        }
        delete2.setOnClickListener{
            profileViewModel.removeGalleryImage(this, 1)
        }
        delete3.setOnClickListener{
            profileViewModel.removeGalleryImage(this, 2)
        }

        addButton.setOnClickListener{
            editingIndex = -1
            val profile = profileViewModel.currentProfile.value
            if (profile != null && profile.images.size < ProfileViewModel.MAX_IMAGES){
                openDialog()
            } else{
                Toast.makeText(this, "Maximum number of images reached", Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.loadProfile {
            profileViewModel.loadGallery()
        }
        profileViewModel.gallery.observe(this){
            bitmaps ->
            // Change this later
            image1.setImageResource(android.R.color.transparent)
            image2.setImageResource(android.R.color.transparent)
            image3.setImageResource(android.R.color.transparent)
            container1.visibility = LinearLayout.GONE
            container2.visibility = LinearLayout.GONE
            container3.visibility = LinearLayout.GONE
            if (bitmaps.size > 0) {
                image1.setImageBitmap(bitmaps[0])
                container1.visibility = LinearLayout.VISIBLE
            }
            if (bitmaps.size > 1){
                image2.setImageBitmap(bitmaps[1])
                container2.visibility = LinearLayout.VISIBLE
            }
            if (bitmaps.size > 2){
                image3.setImageBitmap(bitmaps[2])
                container3.visibility = LinearLayout.VISIBLE
            }
        }
    }

    private fun openDialog(){
        val bundle = Bundle()
        bundle.putString(EditAvatarDialog.DIALOG_NAME, "Upload Image")
        editDialog.arguments = bundle
        editDialog.show(supportFragmentManager, "")
    }

    fun startUseCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
        editDialog.dismiss()
    }

    fun startUseGallery(){
        galleryResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        editDialog.dismiss()
    }
}