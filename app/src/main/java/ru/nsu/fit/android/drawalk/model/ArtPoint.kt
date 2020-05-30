package ru.nsu.fit.android.drawalk.model

import android.location.Location

data class ArtPoint(
    var coords: Location,
    var settings: PointSettings
)