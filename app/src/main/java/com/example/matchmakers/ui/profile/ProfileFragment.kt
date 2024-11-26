package com.example.matchmakers.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
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
import java.io.Serializable
import kotlin.collections.List

class ProfileFragment: Fragment() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var logoutButton: Button
    private lateinit var editAvatarButton: Button
    private lateinit var editInterestsButton: Button
    private lateinit var avatarImage: ImageView
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var biographyInput: EditText
    private lateinit var saveButton: Button

    private var interestsArray = ArrayList<String>()
    private lateinit var interestsList: GridView
    private lateinit var adapter: InterestsAdapter

    private lateinit var editDialog: EditAvatarDialog

    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var interestsResult: ActivityResultLauncher<Intent>
    private lateinit var profileViewModel: ProfileViewModel

    private var loaded = false
    private val LOADED_KEY = "loaded"

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        ImageUtils.checkPermissions(requireActivity())

        val context = requireActivity()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        editDialog = EditAvatarDialog()
        editDialog.profileFragment = this

        logoutButton = root.findViewById(R.id.logout_button)
        editAvatarButton = root.findViewById(R.id.edit_avatar)
        editInterestsButton = root.findViewById(R.id.edit_interests)
        avatarImage = root.findViewById(R.id.avatar_image)
        nameInput = root.findViewById(R.id.profile_name)
        ageInput = root.findViewById(R.id.profile_age)
        biographyInput = root.findViewById(R.id.profile_biography)
        saveButton = root.findViewById(R.id.save_profile)

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

        interestsArray = arrayListOf()
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
        interestsResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK){
                val interests = if (Build.VERSION.SDK_INT >= 33){
                    result.data?.getSerializableExtra("interests", Serializable::class.java) as List<String>
                } else{
                    result.data?.getSerializableExtra("interests") as List<String>
                }

                profileViewModel.currentProfile.value?.interest1 = if (interests.size >= 1) interests[0] else ""
                profileViewModel.currentProfile.value?.interest2 = if (interests.size >= 2) interests[1] else ""
                profileViewModel.currentProfile.value?.interest3 = if (interests.size >= 3) interests[2] else ""

                interestsArray.clear()
                interestsArray.addAll(interests)
                adapter.notifyDataSetChanged()

                saveProfile()

                loaded = true
            }
        }

        editInterestsButton.setOnClickListener{
            val intent = Intent(context, EditInterestsActivity::class.java)
            intent.putExtra(EditInterestsActivity.INTERESTS_KEY, interestsArray.toList() as Serializable)
            interestsResult.launch(intent)
        }

        saveButton.setOnClickListener{
            saveProfile()
        }

        if (savedInstanceState != null){
            loaded = savedInstanceState.getBoolean(LOADED_KEY)
        }
        if (!loaded){
            println("Loading profile")
            profileViewModel.loadProfile{
                nameInput.setText(it.name)
                ageInput.setText(it.age.toString())
                biographyInput.setText(it.biography)

                interestsArray.clear()
                interestsArray.add(it.interest1)
                interestsArray.add(it.interest2)
                interestsArray.add(it.interest3)
                interestsArray.removeIf{b -> b == ""}
                adapter.notifyDataSetChanged()
            }
        }
        val savedInterests = arrayListOf(
            profileViewModel.currentProfile.value?.interest1,
            profileViewModel.currentProfile.value?.interest2,
            profileViewModel.currentProfile.value?.interest3
        )
        println(savedInterests)
        savedInterests.removeIf{b -> b == "" || b == null}
        if (savedInterests.size > 0){
            interestsArray.clear()
            for (i in savedInterests.indices){
                if (savedInterests[i] != null){
                    savedInterests[i]?.let { interestsArray.add(it) }
                }
            }
            adapter.notifyDataSetChanged()
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

    private fun saveProfile(){
        val name = nameInput.text.toString()
        val age = ageInput.text.toString().toIntOrNull()
        val biography = biographyInput.text.toString()

        nameInput.clearFocus()
        ageInput.clearFocus()
        biographyInput.clearFocus()

        val profile = profileViewModel.currentProfile.value
        if (profile != null){
            profileViewModel.currentProfile.value?.name = name
            profileViewModel.currentProfile.value?.age = age ?: 0
            profileViewModel.currentProfile.value?.biography = biography
            profileViewModel.saveProfile()

            Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()
        }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(LOADED_KEY, loaded)
    }
}