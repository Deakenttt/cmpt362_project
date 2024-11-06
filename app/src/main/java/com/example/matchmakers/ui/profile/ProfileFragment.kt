package com.example.matchmakers.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentProfileBinding
import com.example.matchmakers.ui.auth.LoginActivity
import java.io.File

class ProfileFragment: Fragment() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var logoutButton: Button
    private lateinit var editAvatarButton: Button
    private lateinit var avatarImage: ImageView

    private var interestsArray = ArrayList<String>()
    private lateinit var interestsList: GridView
    private lateinit var adapter: InterestsAdapter

    private lateinit var editDialog: EditAvatarDialog

    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<PickVisualMediaRequest>

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        ImageUtils.checkPermissions(requireActivity())

        val context = requireActivity()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        editDialog = EditAvatarDialog()
        editDialog.profileFragment = this

        logoutButton = root.findViewById(R.id.logout_button)
        editAvatarButton = root.findViewById(R.id.edit_avatar)
        avatarImage = root.findViewById(R.id.avatar_image)

        logoutButton.setOnClickListener{
            with (sharedPref.edit()){
                clear()
                remove(LoginActivity.EMAIL_KEY)
                remove(LoginActivity.PASSWORD_KEY)
                apply()
            }
            startActivity(Intent(context, LoginActivity::class.java))
            context.finish()
        }
        editAvatarButton.setOnClickListener{
            val bundle = Bundle()
            bundle.putString(EditAvatarDialog.DIALOG_NAME, "Choose Photo")
            editDialog.arguments = bundle
            editDialog.show(context.supportFragmentManager, "")
        }

        interestsArray = arrayListOf("Interest 1", "Interest 2", "Interest 3", "Interest 4", "Interest 5")
        interestsList = root.findViewById(R.id.profile_interests)
        adapter = InterestsAdapter(context, interestsArray)
        interestsList.adapter = adapter


        val tempImgFile = File(context.getExternalFilesDir(null), "temp_profile_image.jpg")
        tempImgUri = FileProvider.getUriForFile(context, "com.example.matchmakers", tempImgFile)
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK){
                val rotation = ImageUtils.getCameraRotation(context)
                val bitmap = ImageUtils.getBitmap(context, tempImgUri, rotation)
                profileViewModel.replaceAvatar(context, bitmap)
            }
        }
        galleryResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
            uri: Uri? ->
            if (uri != null){
                val bitmap = ImageUtils.getBitmap(context, uri, 90f)
                profileViewModel.replaceAvatar(context, bitmap)
            }
        }

        profileViewModel.avatar.observe(viewLifecycleOwner){
            bitmap -> avatarImage.setImageBitmap(bitmap)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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