package ru.nsu.fit.android.drawalk.utils

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import ru.nsu.fit.android.drawalk.model.MapSegment
import kotlin.math.max
import kotlin.math.min

object MapUtils {
    fun getBounds(segments: List<MapSegment>): LatLngBounds {
        var swLat = 90.0
        var swLng = 180.0
        var neLat = -90.0
        var neLng = -180.0
        for (segment in segments) {
            for (latLng in segment.coordinates) {
                swLat = min(swLat, latLng.latitude)
                swLng = min(swLng, latLng.longitude)
                neLat = max(neLat, latLng.latitude)
                neLng = max(neLng, latLng.longitude)
            }
        }
        if (swLat > neLat || swLng > neLng) {
            return LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        }
        Log.d("BOUNDS", "$swLat $swLng, $neLat $neLng")
        return LatLngBounds(LatLng(swLat, swLng), LatLng(neLat, neLng))
    }
}