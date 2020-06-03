package ru.nsu.fit.android.drawalk.utils.parcelers

import android.os.Parcel
import kotlinx.android.parcel.Parceler
import java.util.*

object DateParceler : Parceler<Date> {
    override fun create(parcel: Parcel) = Date(parcel.readLong())

    override fun Date.write(parcel: Parcel, flags: Int)
            = parcel.writeLong(time)
}