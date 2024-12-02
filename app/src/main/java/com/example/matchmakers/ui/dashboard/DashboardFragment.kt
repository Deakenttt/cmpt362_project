package com.example.matchmakers.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.matchmakers.R
import com.example.matchmakers.database.UserDatabase
import com.example.matchmakers.databinding.FragmentDashboardBinding
import com.example.matchmakers.maplogic.LocationPermissionHelper
import com.example.matchmakers.match.MatchListFragment
import com.example.matchmakers.match.MatchListAdapter
// DK modified
import com.example.matchmakers.repository.LocalUserRepository
import com.example.matchmakers.repository.RemoteUserRepository
import com.example.matchmakers.ui.home.HomeViewModel
import com.example.matchmakers.ui.home.HomeViewModelFactory
import com.example.matchmakers.ui.mapview.MapActivity
import com.example.matchmakers.viewmodel.UserViewModel
import com.example.matchmakers.viewmodel.UserViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var homeViewModel: HomeViewModel
    private val matchListAdapter = MatchListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        initialize()
        return binding.root
    }

    private fun initialize() {
        setupViewModel()
        setupRecyclerView()
        setupButtons()
        setupMap()
    }

    private fun setupViewModel() {
        // get UserDatabase 和 UserRepository
        val userDatabase = UserDatabase.getDatabase(requireContext())
        val userDao = userDatabase.userDao()
        val localUserRepository = LocalUserRepository(userDao)
        val remoteUserRepository = RemoteUserRepository()

        // use UserViewModelFactory create UserViewModel
        val userViewModelFactory = UserViewModelFactory(localUserRepository, remoteUserRepository)
        val userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]

        // use HomeViewModelFactory create HomeViewModel
        val homeViewModelFactory = HomeViewModelFactory(userViewModel)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
    }



    private fun setupRecyclerView() {
        // DK commend
//        binding.matchRecyclerView.adapter = matchListAdapter
//        homeViewModel.getLikedUsers().observe(viewLifecycleOwner) { likedUsers ->
//            matchListAdapter.submitList(likedUsers.takeLast(10))
//        }
    }

    private fun setupButtons() {
//        binding.matchButton.setOnClickListener {
//            navigateToMatchListFragment()
//        }
        binding.mapButton.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupMap() {
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.dashboard_map_container) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.dashboard_map_container, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (LocationPermissionHelper.isLocationPermissionGranted(requireContext())) {
            enableUserLocation()
        } else {
            LocationPermissionHelper.requestLocationPermission(requireActivity())
        }
    }

    private fun enableUserLocation() {
        googleMap.isMyLocationEnabled = true
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            } ?: Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMatchListFragment() {
        // Use NavController to navigate to MatchListFragment
        findNavController().navigate(R.id.action_dashboardFragment_to_matchFragment)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





