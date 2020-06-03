package ru.nsu.fit.android.drawalk.model.firebase

import com.google.firebase.firestore.GeoPoint

data class ArtPolyLine(
    var points: List<GeoPoint> = listOf(),
    var settings: PointSettings = PointSettings()
)