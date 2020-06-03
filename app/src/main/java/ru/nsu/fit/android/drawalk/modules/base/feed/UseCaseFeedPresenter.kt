package ru.nsu.fit.android.drawalk.modules.base.feed

import ru.nsu.fit.android.drawalk.common.UseCase

abstract class UseCaseFeedPresenter<T : Any>(
    private val view: IFeedFragment<T>,
    private val data: List<T?>,
    private val pageSize: Int
) : IFeedPresenter {
    protected abstract val loadUseCase: UseCase<Page, List<T>>
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
        if (loading) {
            return
        }
        loading = true
        loadUseCase.request(Page(data.size, pageSize))
        view.startLoading()
        loadUseCase.execute()

    }

    data class Page(var offset: Int, var limit: Int)
}