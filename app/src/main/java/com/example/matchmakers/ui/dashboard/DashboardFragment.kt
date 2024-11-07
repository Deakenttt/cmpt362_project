package com.example.matchmakers.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchmakers.R
import com.example.matchmakers.maplogic.LocationPermissionHelper
import com.example.matchmakers.ui.mapview.MapActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.dashboard_map_container) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.dashboard_map_container, it).commit()
            }
        mapFragment.getMapAsync(this)

        // Set up button click listener for Map button
        val mapButton = view.findViewById<Button>(R.id.map_button)
        mapButton.setOnClickListener {
            Log.d("MapActivity", "Map button clicked, starting MapActivity")
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivity(intent)
        }


        // Set up button click listener for Match button
        val matchButton = view.findViewById<Button>(R.id.match_button)
        matchButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_matchFragment)

        }

        return view
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
}




