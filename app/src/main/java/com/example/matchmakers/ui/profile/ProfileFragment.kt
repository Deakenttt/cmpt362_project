package com.example.matchmakers.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.R
import com.example.matchmakers.databinding.FragmentProfileBinding
import com.example.matchmakers.ui.auth.LoginActivity

class ProfileFragment: Fragment() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var logoutButton: Button

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

        val context = requireActivity()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        logoutButton = root.findViewById(R.id.logout_button)
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}