package com.example.matchmakers.ui.createaccount

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchmakers.ProfileInfo
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateAccountViewModel(
    private val imageRotationViewModel: ImageRotationViewModel = ImageRotationViewModel()
): ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private val ref = storage.reference


    private val _accountCreated = MutableLiveData<Boolean>()
    val accountCreated: LiveData<Boolean> = _accountCreated

    private val _avatarImage = MutableLiveData<Bitmap?>()
    val avatarImage: LiveData<Bitmap?> = _avatarImage

    private val _galleryImage1 = MutableLiveData<Bitmap?>()
    val galleryImage1: LiveData<Bitmap?> = _galleryImage1

    private val _galleryImage2 = MutableLiveData<Bitmap?>()
    val galleryImage2: LiveData<Bitmap?> = _galleryImage2

    private val _galleryImage3 = MutableLiveData<Bitmap?>()
    val galleryImage3: LiveData<Bitmap?> = _galleryImage3


    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        return if (bitmap.width > targetWidth || bitmap.height > targetHeight) {
            if (bitmap.width > bitmap.height) {
                val scaledHeight = (targetWidth / aspectRatio).toInt()
                Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)
            } else {
                val scaledWidth = (targetHeight * aspectRatio).toInt()
                Bitmap.createScaledBitmap(bitmap, scaledWidth, targetHeight, true)
            }
        } else {
            bitmap // No resizing needed if within limits
        }
    }

    // Update avatar image with rotation correction
    fun updateAvatarImage(context: Context, uri: Uri, targetWidth: Int = 800, targetHeight: Int = 800) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val rotatedBitmap = imageRotationViewModel.rotateBitmapIfRequiredSync(context, bitmap, uri)
                val resizedBitmap = resizeBitmap(rotatedBitmap, targetWidth, targetHeight)
                withContext(Dispatchers.Main) {
                    _avatarImage.value = resizedBitmap
                }
            } catch (e: Exception) {
                Log.e("CreateAccountViewModel", "Error updating avatar image", e)
            }
        }
    }

    // Update gallery image 1 with rotation correction
    fun updateGalleryImage1(context: Context, uri: Uri, targetWidth: Int = 800, targetHeight: Int = 800) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val rotatedBitmap = imageRotationViewModel.rotateBitmapIfRequiredSync(context, bitmap, uri)
                val resizedBitmap = resizeBitmap(rotatedBitmap, targetWidth, targetHeight) // Example size
                withContext(Dispatchers.Main) {
                    _galleryImage1.value = resizedBitmap
                }
            } catch (e: Exception) {
                Log.e("CreateAccountViewModel", "Error updating gallery image 1", e)
            }
        }
    }

    // Update gallery image 2 with rotation correction
    fun updateGalleryImage2(context: Context, uri: Uri, targetWidth: Int = 800, targetHeight: Int = 800) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val rotatedBitmap = imageRotationViewModel.rotateBitmapIfRequiredSync(context, bitmap, uri)
                val resizedBitmap = resizeBitmap(rotatedBitmap, targetWidth, targetHeight) // Example size
                withContext(Dispatchers.Main) {
                    _galleryImage2.value = resizedBitmap
                }
            } catch (e: Exception) {
                Log.e("CreateAccountViewModel", "Error updating gallery image 2", e)
            }
        }
    }

    // Update gallery image 3 with rotation correction
    fun updateGalleryImage3(context: Context, uri: Uri, targetWidth: Int = 800, targetHeight: Int = 800) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val rotatedBitmap = imageRotationViewModel.rotateBitmapIfRequiredSync(context, bitmap, uri)
                val resizedBitmap = resizeBitmap(rotatedBitmap, targetWidth, targetHeight) // Example size
                withContext(Dispatchers.Main) {
                    _galleryImage3.value = resizedBitmap
                }
            } catch (e: Exception) {
                Log.e("CreateAccountViewModel", "Error updating gallery image 3", e)
            }
        }
    }


//    fun updateAvatarImage(bitmap: Bitmap) {
//        _avatarImage.value = bitmap
//    }
//
//    fun updateGalleryImage1(bitmap: Bitmap) {
//        _galleryImage1.value = bitmap
//    }
//
//    fun updateGalleryImage2(bitmap: Bitmap) {
//        _galleryImage2.value = bitmap
//    }
//
//    fun updateGalleryImage3(bitmap: Bitmap) {
//        _galleryImage3.value = bitmap
//    }

    fun createAccount(
        name: String,
        age: Int,
        interest1: String,
        interest2: String,
        interest3: String,
        biography: String,
        gender: String,
        context: Context
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val genderString = when(gender){
                "Male" -> "M"
                "Female" -> "F"
                else -> ""
            }
            val userInfo = hashMapOf(
                "interest" to interest1,
                "latitude" to 0.0,
                "longitude" to 0.0,
                "cluster" to 0,
                "gender" to genderString,
                "name" to name,
                "recommended" to emptyList<Int>(),
            )

            db.collection("users").document(uid)
                .set(userInfo)
                .addOnSuccessListener {
                    _accountCreated.value = true
                }
                .addOnFailureListener {
                    _accountCreated.value = false
                }

            // Collect images
            val imagesList = mutableListOf<String>()

            // Upload avatar image (but do not add to imagesList)
            avatarImage.value?.let { bitmap ->
                val avatarPath = "avatars/$uid.jpg"
                uploadImage(context, bitmap, avatarPath) {}
            }
            galleryImage1.value?.let { bitmap ->
                val galleryPath1 = "gallery/$uid/image_1.jpg"
                uploadImage(context, bitmap, galleryPath1) {}
            }
            galleryImage2.value?.let { bitmap ->
                val galleryPath2 = "gallery/$uid/image_2.jpg"
                uploadImage(context, bitmap, galleryPath2) {}
            }
            galleryImage3.value?.let { bitmap ->
                val galleryPath3 = "gallery/$uid/image_3.jpg"
                uploadImage(context, bitmap, galleryPath3) {}
            }

            imagesList.add("image_1.jpg")
            imagesList.add("image_2.jpg")
            imagesList.add("image_3.jpg")

            val calendar = Calendar.getInstance()
            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            println("mgs: imageslist $imagesList")
            val profileInfo = hashMapOf(
                "name" to name,
                "age" to age,
                "gender" to gender,
                "interest1" to interest1,
                "interest2" to interest2,
                "interest3" to interest3,
                "images" to imagesList,  // Storing all image paths here
                "biography" to biography,
                "timestamp" to timestamp,
                "DislikeList" to emptyList<String>(),
                "DislikedMe" to emptyList<String>(),
                "LikeList" to emptyList<String>(),
                "LikedMe" to emptyList<String>(),
                "MatchedMe" to emptyList<String>(),
            )

            // Update profileinfo collection to store all insensitive user data
            db.collection("profileinfo").document(uid)
                .set(profileInfo)
                .addOnSuccessListener {
                    println("Profile info saved")
                }
                .addOnFailureListener {
                    println("Failed to save profile info")
                }

        } else {
            _accountCreated.value = false
        }
    }

    private fun uploadImage(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        callback: () -> Unit
    ) {
        val file = ref.child(fileName)
        val baoS = ByteArrayOutputStream()

        if (fileName.endsWith(".png")) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baoS)
        } else if (fileName.endsWith(".jpg")) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baoS)
        }

        val data = baoS.toByteArray()

        val uploadTask = file.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            //Toast.makeText(context, "Image successfully uploaded", Toast.LENGTH_SHORT).show()
            callback() // Call the callback once the image is uploaded
        }
    }

    fun correctImageRotation(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        return imageRotationViewModel.rotateBitmapIfRequiredSync(context, bitmap, uri)
    }

}
