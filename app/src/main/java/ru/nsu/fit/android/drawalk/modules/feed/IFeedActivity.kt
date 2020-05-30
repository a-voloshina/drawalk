package ru.nsu.fit.android.drawalk.modules.feed

import ru.nsu.fit.android.drawalk.modules.base.IViewActivity

abstract class IFeedActivity<T: Any>: IViewActivity<IFeedPresenter>() {
    abstract fun startLoading()
    abstract fun updateFeed(newData: List<T>)
    abstract fun showError(cause: Throwable)
}