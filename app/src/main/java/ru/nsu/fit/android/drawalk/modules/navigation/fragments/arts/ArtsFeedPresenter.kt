package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.base.feed.FirebaseFeedPresenter
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

open class ArtsFeedPresenter(
    view: IFeedFragment<GpsArtData>,
    data: List<GpsArtData?>,
    pageSize: Int = 10
) : FirebaseFeedPresenter<GpsArtData>(view, data, pageSize) {
    override fun getDataCapacity() {
        FirebaseHolder.getArtsSize(
            onSuccess = { view.setDataCapacity(it) },
            onError = { view.showError(it) }
        )
    }

    override fun queryData() {
        FirebaseHolder.getArtsPage(
            pageSize.toLong(),
            lastVisible,
            updateAfter = { lastVisible = it },
            onSuccess = { view.updateFeed(it) },
            onError = { view.showError(it) }
        )
    }
}