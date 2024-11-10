package com.example.matchmakers.mapslogic

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.matchmakers.UserInfo
import com.google.android.gms.maps.CameraUpdateFactory

class MapDataHandler {

    fun addUsersMarkers(map: GoogleMap, users: List<UserInfo>) {
        for (user in users) {
            val position = LatLng(user.latitude, user.longitude)
            val markerOptions = MarkerOptions().position(position).title(user.name)
            map.addMarker(markerOptions)
        }
    }

    fun createMarkerOptions(position: LatLng, title: String): MarkerOptions {
        return MarkerOptions().position(position).title(title)
    }

    fun setCameraToUserLocation(map: GoogleMap, userLocation: LatLng, zoomLevel: Float = 12f) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel))
    }
}



