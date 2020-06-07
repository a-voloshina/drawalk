package ru.nsu.fit.android.drawalk.modules.base.feed

import ru.nsu.fit.android.drawalk.modules.base.IPresenter

interface IFeedPresenter: IPresenter {
    fun loadMoreData()

    fun getDataCapacity()
}