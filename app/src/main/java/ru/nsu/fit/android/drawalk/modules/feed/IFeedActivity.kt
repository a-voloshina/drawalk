package ru.nsu.fit.android.drawalk.modules.feed

import ru.nsu.fit.android.drawalk.modules.base.IViewActivity

abstract class IFeedActivity: IViewActivity<IFeedPresenter>() {
    abstract fun startLoading()
    abstract fun updateFeed(start: Int, count: Int)
}