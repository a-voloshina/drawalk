package ru.nsu.fit.android.drawalk.modules.artdetails

import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.modules.base.IViewActivity

abstract class IArtDetailsActivity: IViewActivity<IArtDetailsPresenter>() {
    abstract fun updateArtData(art: GpsArt, username: String)

    abstract fun updateArtName(name: String)

    abstract fun closeOnArtDeleted()
}