package com.example.matchmakers.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.matchmakers.databinding.FragmentMatchListBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.matchmakers.database.UserDatabase
// DK modified
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.repository.RemoteUserRepository
import com.example.matchmakers.ui.home.HomeViewModel
import com.example.matchmakers.ui.home.HomeViewModelFactory
import com.example.matchmakers.viewmodel.UserViewModel
import com.example.matchmakers.viewmodel.UserViewModelFactory


class MatchListFragment : Fragment() {

    private var _binding: FragmentMatchListBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchListBinding.inflate(inflater, container, false)
        initialize()
        return binding.root
    }

    private fun initialize() {
        setupViewModel()
        setupListView()
    }

    private fun setupViewModel() {
        // get UserViewModel
        val userDatabase = UserDatabase.getDatabase(requireContext())
        val userDao = userDatabase.userDao()
        val localUserRepository = LocalUserRepository(userDao)
        val remoteUserRepository = RemoteUserRepository()
        val userViewModelFactory = UserViewModelFactory(localUserRepository, remoteUserRepository)
        val userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]

        // use HomeViewModelFactory to create HomeViewModel
        val homeViewModelFactory = HomeViewModelFactory(userViewModel)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
    }

    private fun setupListView() {
        // DK commend
        // Observe liked users and set them in the ListView
//        homeViewModel.getLikedUsers().observe(viewLifecycleOwner) { likedUsers ->
//            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, likedUsers.map { it.name })
//            binding.matchList.adapter = adapter
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

