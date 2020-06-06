package ru.nsu.fit.android.drawalk.model

import com.google.android.gms.maps.model.LatLng

data class MapSegment(
    val coordinates: MutableList<LatLng> = ArrayList(),
    var color: Int = 0,
    var width: Float = 1.0f
)