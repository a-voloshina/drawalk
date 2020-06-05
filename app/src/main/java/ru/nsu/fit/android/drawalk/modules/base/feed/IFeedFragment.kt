package ru.nsu.fit.android.drawalk.modules.base.feed

import ru.nsu.fit.android.drawalk.modules.base.IViewFragment

abstract class IFeedFragment<T: Any>(layoutId: Int): IViewFragment<IFeedPresenter>(layoutId) {
    abstract fun startLoading()
    abstract fun updateFeed(newData: List<T>)
    abstract fun setDataCapacity(cap: Int?)
}