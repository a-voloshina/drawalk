package ru.nsu.fit.android.drawalk.modules.feed

class FeedPresenter(
    private val view: IFeedActivity,
    pageSize: Int = 10
) : IFeedPresenter {
    private val loadData = LoadData(pageSize)
    private var loading = false

    override fun loadMoreData() {
        load()
    }

    override fun start() {
        load()
    }

    private fun load() {
        if (!loading) {
            loading = true
            view.startLoading()
            loadData.onComplete {
                loading = false
                view.updateFeed(it)
            }.execute()
        }
    }
}