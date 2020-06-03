package ru.nsu.fit.android.drawalk.modules.base.feed

import com.google.firebase.firestore.DocumentSnapshot

abstract class FirebaseFeedPresenter<T: Any>(
    protected val view: IFeedFragment<T>,
    protected val data: List<T?>,
    protected val pageSize: Int = 10
): IFeedPresenter {
    protected var lastVisible: DocumentSnapshot? = null
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
        if (loading) {
            return
        }
        loading = true
        view.startLoading()
        queryData()
    }

    protected fun endWithError(e: Throwable) {
        loading = false
        view.showError(e)
    }

    protected fun endWithData(data: List<T>) {
        loading = false
        view.updateFeed(data)
    }

    abstract fun queryData()
}