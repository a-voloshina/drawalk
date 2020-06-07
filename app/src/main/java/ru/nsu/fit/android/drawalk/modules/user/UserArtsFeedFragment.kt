package ru.nsu.fit.android.drawalk.modules.user

import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts.ArtsFeedFragment

open class UserArtsFeedFragment(private val userId: String? = null): ArtsFeedFragment() {
    override fun providePresenter(
        view: IFeedFragment<GpsArtData>,
        data: List<GpsArtData?>
    ) =
        UserArtsFeedPresenter(
            userId,
            view,
            data
        )

    override val noDataText by lazy { getString(R.string.no_user_arts_text) }
}