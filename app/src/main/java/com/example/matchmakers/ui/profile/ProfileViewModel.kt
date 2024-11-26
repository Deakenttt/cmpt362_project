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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileViewModel: ViewModel() {
    private val avatarPath = "avatars/"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private val ref = storage.reference

    val avatar = MutableLiveData<Bitmap>()
    val otherAvatar = MutableLiveData<Bitmap>()

    val currentProfile = MutableLiveData<ProfileInfo>()
    val otherProfile = MutableLiveData<ProfileInfo>()

    init{
        val user = auth.currentUser
        if (user != null){
            val path = getPath(user.uid)
            if (path != ""){
                downloadBytes(path, true)
            }
        }
    }

    private fun getPath(uid: String): String{
        // User's id is unique so it is used for the image file in the storage bucket
        // Return immediately if there is somehow no user logged in...
        if (uid == "") return ""
        val path = "$avatarPath${uid}.jpg"
        return path
    }

    private fun downloadBytes(fileName: String, currentUser: Boolean){
        val task = ref.child(fileName).getBytes(1073741824)
        task.addOnSuccessListener{
            val j = it
            val bitmap = BitmapFactory.decodeByteArray(j, 0, j.size)
            if (currentUser){
                avatar.value = bitmap
            } else{
                otherAvatar.value = bitmap
            }
        }
        task.addOnFailureListener{
            println("failed to load, user probably doesn't have an avatar")
        }
    }

    private fun uploadImage(context: Context, bitmap: Bitmap, fileName: String) {
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
            downloadBytes(fileName, true)
        }
    }

    fun replaceAvatar(context: Context, bitmap: Bitmap){
        // Uploads the new image inside the bitmap and replaces the current avatar with that bitmap
        val user = auth.currentUser ?: return
        val path = "$avatarPath${user.uid}.jpg"
        uploadImage(context, bitmap, path)
    }

    fun updateOtherAvatar(context: Context, uid: String){
        val path = getPath(uid)
        downloadBytes(path, false)
    }

    fun loadOtherProfile(uid: String){
        db.collection("profileinfo").document(uid).get().addOnSuccessListener{
            val name = it.getString("name") ?: "Unknown User"
            val age = it.getLong("age")?.toInt() ?: 0
            val gender = it.getString("gender") ?: ""
            val interest1 = it.getString("interest1") ?: ""
            val interest2 = it.getString("interest2") ?: ""
            val interest3 = it.getString("interest3") ?: ""
            val biography = it.getString("biography") ?: ""

            otherProfile.value = ProfileInfo(name, age, gender, interest1, interest2, interest3, biography)
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
            val biography = it.getString("biography") ?: ""

            val profile = ProfileInfo(name, age, gender, interest1, interest2, interest3, biography)
            callback(profile)
            currentProfile.value = profile
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
}