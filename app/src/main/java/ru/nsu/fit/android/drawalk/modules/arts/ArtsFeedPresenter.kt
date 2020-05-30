package ru.nsu.fit.android.drawalk.modules.arts

import ru.nsu.fit.android.drawalk.model.GpsArt
import ru.nsu.fit.android.drawalk.modules.feed.FeedPresenter
import ru.nsu.fit.android.drawalk.modules.feed.IFeedActivity

class ArtsFeedPresenter(view: IFeedActivity<GpsArt>, pageSize: Int = 10): FeedPresenter<GpsArt>(view) {
    override val loadUseCase =
        LoadData(pageSize)
}