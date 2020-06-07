package ru.nsu.fit.android.drawalk.model.firebase

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(
    var id: String = "",
    var name: String = "",
    var imageUrl: String? = null
): Parcelable