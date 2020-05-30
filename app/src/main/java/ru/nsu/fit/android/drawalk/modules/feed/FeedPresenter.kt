package ru.nsu.fit.android.drawalk.modules.feed

import ru.nsu.fit.android.drawalk.model.GpsArt

class FeedPresenter(
    private val view: IFeedActivity,
    private val data: MutableList<GpsArt?> = mutableListOf(),
    private val pageSize: Int = 10
): IFeedPresenter {
    private val loadData = LoadData(LoadData.LoadRequest(data, pageSize))
    private var loading = false

    override fun loadMoreData() {
        load()
    }

    override fun start() {
        if (data.isEmpty()) {
            load()
        }
    }

    private fun load() {
        if (!loading) {
            loading = true
            view.startLoading()
            loadData.onComplete {
                view.updateFeed(data.size, pageSize)
                loading = false
            }.execute()
        }
    }
}