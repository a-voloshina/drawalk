package ru.nsu.fit.android.drawalk.model.firebase

import com.google.firebase.Timestamp

data class GpsArt(
    var id: String = "",
    var authorId: String = "",
    var name: String = "",
    var parts: List<ArtPolyLine> = listOf(),
    var created: Timestamp = Timestamp.now(),
    var previewUrl: String? = null
)