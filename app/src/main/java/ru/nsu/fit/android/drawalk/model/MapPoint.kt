package ru.nsu.fit.android.drawalk.model

import com.google.android.gms.maps.model.LatLng

data class MapPoint(
    val coordinate: LatLng = LatLng(0.0, 0.0),
    var color: Int = 0,
    var width: Float = 1.0f
) {
}