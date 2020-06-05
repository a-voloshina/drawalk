package ru.nsu.fit.android.drawalk.modules.artdetails

import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class ArtDetailsPresenter(
    private val view: IArtDetailsActivity,
    private val artId: String?
): IArtDetailsPresenter {
    override fun start() {
        if (artId == null) {
            view.showError(RuntimeException("Art not found"))
            return
        }
        FirebaseHolder.getArtFullInfo(artId, { art, username ->
            view.updateArtData(art, username ?: "NOT FOUND")
        }) {
            view.showError(it)
        }
    }

    override fun renameArt(name: String) {
        if (artId == null) {
            view.showError(RuntimeException("Art not found"))
            return
        }
        FirebaseHolder.renameArt(artId, name, {
            view.updateArtName(name)
        }) {
            view.showError(it)
        }
    }

    override fun deleteArt() {
        if (artId == null) {
            view.showError(RuntimeException("Art not found"))
            return
        }
        FirebaseHolder.deleteArt(artId, {
            view.closeOnArtDeleted()
        }) {
            view.showError(it)
        }
    }
}