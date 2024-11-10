package com.example.matchmakers.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class ProfileViewModel: ViewModel() {
    private val avatarPath = "avatars/"

    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage
    private val ref = storage.reference

    val avatar = MutableLiveData<Bitmap>()

    init{
        val path = getPath()
        if (path != ""){
            downloadBytes(path)
        }
    }

    private fun getPath(): String{
        // User's id is unique so it is used for the image file in the storage bucket
        // Return immediately if there is somehow no user logged in...
        val user = auth.currentUser ?: return ""
        val path = "$avatarPath${user.uid}.jpg"
        return path
    }

    private fun downloadBytes(fileName: String){
        val task = ref.child(fileName).getBytes(1073741824)
        task.addOnSuccessListener{
            val j = it
            val bitmap = BitmapFactory.decodeByteArray(j, 0, j.size)
            avatar.value = bitmap
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
            downloadBytes(fileName)
        }
    }

    fun replaceAvatar(context: Context, bitmap: Bitmap){
        // Uploads the new image inside the bitmap and replaces the current avatar with that bitmap
        val user = auth.currentUser ?: return
        val path = "$avatarPath${user.uid}.jpg"
        uploadImage(context, bitmap, path)
    }
}