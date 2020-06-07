package ru.nsu.fit.android.drawalk.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.utils.parcelers.DateParceler
import java.util.*

@Parcelize
@TypeParceler<Date, DateParceler>
data class GpsArtData(
    var id: String,
    var name: String,
    var authorId: String,
    var authorName: String,
    var created: Date,
    var imageUrl: String?
): Parcelable {
    constructor(art: GpsArt, authorName: String): this(
        art.id,
        art.name,
        art.authorId,
        authorName,
        art.created.toDate(),
        art.previewUrl
    )
}