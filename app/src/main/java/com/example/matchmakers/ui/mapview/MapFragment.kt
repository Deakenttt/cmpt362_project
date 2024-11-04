package com.example.matchmakers.ui.mapview

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.matchmakers.R
import com.example.matchmakers.mapslogic.LocationPermissionHelper
import com.example.matchmakers.mapslogic.LocationService
import com.example.matchmakers.mapslogic.MapDataHandler
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationService: LocationService
    private val mapDataHandler = MapDataHandler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // initialize location services
        locationService = LocationService(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // check location permission and fetch last known location
        if (LocationPermissionHelper.isLocationPermissionGranted(requireContext())) {
            locationService.getLastKnownLocation { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(mapDataHandler.createMarkerOptions(userLocation, "You are here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                } else {
                    Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            LocationPermissionHelper.requestLocationPermission(requireActivity())
        }
    }
}

