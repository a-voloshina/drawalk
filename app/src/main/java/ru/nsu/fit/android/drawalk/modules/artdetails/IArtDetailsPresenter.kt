package ru.nsu.fit.android.drawalk.modules.artdetails

import ru.nsu.fit.android.drawalk.modules.base.IPresenter

interface IArtDetailsPresenter: IPresenter {
    fun renameArt(name: String)
    fun deleteArt()
}