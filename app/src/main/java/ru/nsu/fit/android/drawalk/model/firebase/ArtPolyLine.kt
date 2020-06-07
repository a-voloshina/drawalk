package ru.nsu.fit.android.drawalk.model.firebase

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import ru.nsu.fit.android.drawalk.model.MapSegment

data class ArtPolyLine(
    var points: List<GeoPoint> = listOf(),
    var settings: PointSettings = PointSettings()
) {
    constructor(segment: MapSegment): this(
        segment.coordinates.map { GeoPoint(it.latitude, it.longitude) },
        PointSettings(
            segment.color,
            segment.width.toDouble()
        )
    )

    fun toMapSegment() = MapSegment(
        points.map { LatLng(it.latitude, it.longitude) }.toMutableList(),
        settings.color,
        settings.width.toFloat()
    )
}