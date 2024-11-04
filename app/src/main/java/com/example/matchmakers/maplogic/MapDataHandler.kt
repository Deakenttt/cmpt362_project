package com.example.matchmakers.mapslogic

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapDataHandler {

    fun createMarkerOptions(position: LatLng, title: String): MarkerOptions {
        return MarkerOptions().position(position).title(title)
    }

    fun getZoomLevelBasedOnDistance(distanceInKm: Double): Float {
        return when {
            distanceInKm < 1 -> 15f
            distanceInKm < 5 -> 13f
            distanceInKm < 10 -> 11f
            else -> 10f
        }
    }
}
