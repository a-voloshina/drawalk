package ru.nsu.fit.android.drawalk.modules.user

import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts.ArtsFeedPresenter
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class UserArtsFeedPresenter(
    private val userId: String?,
    view: IFeedFragment<GpsArtData>,
    data: List<GpsArtData?>,
    pageSize: Int = 10
) : ArtsFeedPresenter(view, data, pageSize) {
    override fun getDataCapacity() {
        FirebaseHolder.getArtsSize(userId, {
            view.setDataCapacity(it)
        }) {
            view.showError(it)
        }
    }

    override fun queryData() {
        FirebaseHolder.getArtsPage(
            pageSize.toLong(),
            lastVisible,
            userId,
            { lastVisible = it },
            { view.updateFeed(it) }
        ) {
            view.showError(it)
        }
    }
}