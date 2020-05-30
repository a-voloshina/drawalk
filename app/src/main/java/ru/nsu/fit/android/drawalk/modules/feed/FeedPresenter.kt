package ru.nsu.fit.android.drawalk.modules.feed

import ru.nsu.fit.android.drawalk.common.UseCase

abstract class FeedPresenter<T: Any>(
    private val view: IFeedActivity<T>
) : IFeedPresenter {
    protected  abstract val loadUseCase: UseCase<Int, List<T>>
    private var loading = false

    protected fun initCallbacks() {
        loadUseCase.onComplete {
            loading = false
            view.updateFeed(it)
        }.onError {
            loading = false
            view.showError(it)
        }
    }

    override fun start() {
        initCallbacks()
        load()
    }

    override fun loadMoreData() {
        load()
    }

    private fun load() {
        if (!loading) {
            loading = true
            view.startLoading()
            loadUseCase.execute()
        }
    }
}