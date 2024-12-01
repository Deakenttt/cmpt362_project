package com.example.matchmakers.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.matchmakers.ProfileInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileViewModel: ViewModel() {
    private val avatarPath = "avatars/"
    private val galleryPath = "gallery/"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private val ref = storage.reference

    val avatar = MutableLiveData<Bitmap>()
    val otherAvatar = MutableLiveData<Bitmap>()

    val gallery = MutableLiveData<ArrayList<Bitmap?>>(arrayListOf())

    val currentProfile = MutableLiveData<ProfileInfo>()
    val otherProfile = MutableLiveData<ProfileInfo>()


    private fun avatarImagePath(uid: String): String{
        // User's id is unique so it is used for the image file in the storage bucket
        // Return immediately if there is somehow no user logged in...
        if (uid == "") return ""
        val path = "$avatarPath${uid}.jpg"
        return path
    }

    private fun galleryImagePath(uid: String, image: String): String{
        if (uid == "" || image == "") return ""
        val path = "$galleryPath${uid}/$image"
        return path
    }

    private fun generateImageName(): String{
        return "image_${System.currentTimeMillis()}.jpg"
    }

    private fun downloadBytes(fileName: String, callback: (Bitmap) -> Unit){
        val task = ref.child(fileName).getBytes(1073741824)
        task.addOnSuccessListener{
            val j = it
            val bitmap = BitmapFactory.decodeByteArray(j, 0, j.size)
            callback(bitmap)
//            if (currentUser){
//                avatar.value = bitmap
//            } else{
//                otherAvatar.value = bitmap
//            }
        }
        task.addOnFailureListener{
            println("failed to load, user probably doesn't have an avatar")
        }
    }

    private fun uploadImage(context: Context, bitmap: Bitmap, fileName: String, callback: () -> Unit){
        val file = ref.child(fileName)
        val baoS = ByteArrayOutputStream()

        if (fileName.endsWith(".png")){
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baoS)
        } else if (fileName.endsWith(".jpg")){
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baoS)
        }

        val data = baoS.toByteArray()

        print ("size ${data.size}")

        val uploadTask = file.putBytes(data)
        uploadTask.addOnFailureListener{
            println("??????????? $it")
            Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            println(taskSnapshot.metadata)
            Toast.makeText(context, "Image successfully uploaded", Toast.LENGTH_SHORT).show()
            callback()
        }
    }

    private fun deleteImage(context: Context, fileName: String, callback: () -> Unit){
        val task = ref.child(fileName).delete()
        task.addOnSuccessListener{
            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
            callback()
        }.addOnFailureListener{
            println("Failed to delete")
        }
    }

    fun loadAvatar(){
        val user = auth.currentUser
        if (user != null){
            val path = avatarImagePath(user.uid)
            if (path != ""){
                downloadBytes(path) {bitmap -> avatar.value = bitmap}
            }
        }
    }

    fun loadGallery(){
        val user = auth.currentUser ?: return
        val profile = currentProfile.value ?: return

//        val path = galleryImagePath(user.uid, profile.image1)
//        if (path == "") return
//
//        downloadBytes(path) {bitmap ->
//            galleryImage1.value = bitmap
//        }
        var loaded = 0
        var required = 0

        val bitmaps: ArrayList<Bitmap?> = arrayListOf()
        val paths = profile.images.map{
            bitmaps.add(null)
            if (it != ""){
                required++
            }
            galleryImagePath(user.uid, it)
        }

        for (i in paths.indices){
            if (paths[i] != ""){
                downloadBytes(paths[i]) {bitmap ->
                    bitmaps[i] = bitmap
                    loaded++
                    if (loaded >= required){
                        // Only modify the mutable live data once all required images have finished loading
                        // otherwise the activity gets an incomplete list of images
                        gallery.value = bitmaps
                    }
                }
            }
        }
        //gallery.value = bitmaps
    }

    fun replaceGalleryImage(context: Context, bitmap: Bitmap, index: Int){
        val user = auth.currentUser ?: return
        val profile = currentProfile.value ?: return

        val bitmaps = gallery.value
        if (bitmaps == null || index < 0 || index >= bitmaps.size || index >= profile.images.size) return

        val list = profile.images.toMutableList()
        if (list[index] == ""){
            // If the current image name is invalid, generate a new one
            // Otherwise, overwrite the existing image with the new image without changing its name
            list[index] = generateImageName()
            currentProfile.value?.images = list.toList()
        }
        val fileName = list[index]

        val path = galleryImagePath(user.uid, fileName)
        if (path == "") return

        println("editing image at $path")
        uploadImage(context, bitmap, path){
            saveProfile()
            downloadBytes(path) {bitmap ->
                bitmaps[index] = bitmap
                gallery.value = bitmaps!!
            }
        }


        /*
        // 1 image only
        var fileName = ""
        var updateBitmap: MutableLiveData<Bitmap>? = null

        when(index){
            0 -> {
                if (profile.image1 == ""){
                    profile.image1 = "image_${System.currentTimeMillis()}.jpg"
                    currentProfile.value?.image1 = profile.image1
                }
                fileName = profile.image1
                updateBitmap = galleryImage1
            }
        }

//        if (profile.image1 == ""){
//            profile.image1 = "image_${System.currentTimeMillis()}.jpg"
//            currentProfile.value?.image1 = profile.image1
//        }

        val path = galleryImagePath(user.uid, fileName)
        if (path == "" || updateBitmap == null) return

        println("PATH $path")
        uploadImage(context, bitmap, path){
            downloadBytes(path) {bitmap -> updateBitmap.value = bitmap}
        }*/
    }

    fun addGalleryImage(context: Context, bitmap: Bitmap){
        val user = auth.currentUser ?: return
        val profile = currentProfile.value ?: return

        val bitmaps = gallery.value
        if (bitmaps == null || bitmaps.size >= MAX_IMAGES || profile.images.size != bitmaps.size) return

        // Initialize empty image
        val list = profile.images.toMutableList()
        list.add(generateImageName())
        bitmaps.add(null)
        currentProfile.value?.images = list.toList()

        saveProfile()

        // Replace the new empty image with the provided bitmap
        replaceGalleryImage(context, bitmap, list.size - 1)

        println("${list.size} ${bitmaps.size}")
        println("${list}")
        println("${bitmaps}")
    }

    fun removeGalleryImage(context: Context, index: Int){
        val user = auth.currentUser ?: return
        val profile = currentProfile.value ?: return

        val bitmaps = gallery.value
        if (bitmaps == null || index < 0 || index >= bitmaps.size || index >= profile.images.size) return

        val list = profile.images.toMutableList()
        val fileName = list[index]
        list.removeAt(index)
        bitmaps.removeAt(index)
        currentProfile.value?.images = list.toList()

        saveProfile()

        val path = galleryImagePath(user.uid, fileName)
        if (path != ""){
            // Remove the deleted image from cloud storage to avoid accumulating unused files
            deleteImage(context, path){}
        }

        gallery.value = bitmaps!!

        println("${list.size} ${bitmaps.size}")
        println("${list}")
        println("${bitmaps}")
    }

    fun replaceAvatar(context: Context, bitmap: Bitmap){
        // Uploads the new image inside the bitmap and replaces the current avatar with that bitmap
        val user = auth.currentUser ?: return
        val path = avatarImagePath(user.uid)
        if (path == "") return
        uploadImage(context, bitmap, path){
            downloadBytes(path) {bitmap -> avatar.value = bitmap}
        }
    }

    fun updateOtherAvatar(context: Context, uid: String){
        val path = avatarImagePath(uid)
        downloadBytes(path) {bitmap -> otherAvatar.value = bitmap}
    }

    fun loadOtherProfile(uid: String){
        db.collection("profileinfo").document(uid).get().addOnSuccessListener{
            val name = it.getString("name") ?: "Unknown User"
            val age = it.getLong("age")?.toInt() ?: 0
            val gender = it.getString("gender") ?: ""
            val interest1 = it.getString("interest1") ?: ""
            val interest2 = it.getString("interest2") ?: ""
            val interest3 = it.getString("interest3") ?: ""
            val images = it.get("images") as List<String>
            val biography = it.getString("biography") ?: ""
            val timestamp = it.getString("timestamp") ?: ""

            otherProfile.value = ProfileInfo(name, age, gender, interest1, interest2, interest3, images, biography, timestamp)
        }.addOnFailureListener{error -> println(error)}
    }

    fun loadProfile(callback: (ProfileInfo) -> Unit){
        val user = auth.currentUser ?: return
        db.collection("profileinfo").document(user.uid).get().addOnSuccessListener{
            val name = it.getString("name") ?: "Unknown User"
            val age = it.getLong("age")?.toInt() ?: 0
            val gender = it.getString("gender") ?: ""
            val interest1 = it.getString("interest1") ?: ""
            val interest2 = it.getString("interest2") ?: ""
            val interest3 = it.getString("interest3") ?: ""
            val images = it.get("images") as List<String>
            val biography = it.getString("biography") ?: ""
            val timestamp = it.getString("timestamp") ?: ""

            val profile = ProfileInfo(name, age, gender, interest1, interest2, interest3, images, biography, timestamp)
            currentProfile.value = profile
            callback(profile)
        }.addOnFailureListener{error -> println(error)}
    }

    fun saveProfile(){
        val user = auth.currentUser ?: return
        val profile = currentProfile.value ?: return
        val calendar = Calendar.getInstance()
        val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val profileMap = hashMapOf(
            "name" to profile.name,
            "age" to profile.age,
            "gender" to profile.gender,
            "interest1" to profile.interest1,
            "interest2" to profile.interest2,
            "interest3" to profile.interest3,
            "images" to profile.images,
            "biography" to profile.biography,
            "timestamp" to timestamp
        )
        db.collection("profileinfo").document(user.uid).set(profileMap).addOnSuccessListener{
            println("Updated profile")
        }.addOnFailureListener{ error ->
            println("Could not update profile: $error")
        }

//        db.collection("users").document(user.uid).update("timestamp", timestamp).addOnSuccessListener {
//            println("updated timestamp")
//        }.addOnFailureListener{ error->
//            println("Could not update timestamp: $error")
//        }
    }

    companion object{
        const val MAX_IMAGES = 3
    }
}