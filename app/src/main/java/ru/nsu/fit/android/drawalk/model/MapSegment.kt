package ru.nsu.fit.android.drawalk.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MapSegment(
    val coordinates: MutableList<LatLng> = ArrayList(),
    var color: Int = 0,
    var width: Float = 1.0f
) : Parcelable