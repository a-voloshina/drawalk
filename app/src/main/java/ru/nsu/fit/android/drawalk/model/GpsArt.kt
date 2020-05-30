package ru.nsu.fit.android.drawalk.model

data class GpsArt(
    var id: String,
    var authorId: String,
    var points: MutableList<ArtPoint>
)