package ru.nsu.fit.android.drawalk.modules.navigation.fragments.users

import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.modules.base.feed.FirebaseFeedPresenter
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class UsersFeedPresenter(
    view: IFeedFragment<UserData>,
    data: List<UserData?>,
    pageSize: Int = 10
) : FirebaseFeedPresenter<UserData>(view, data, pageSize) {
    override fun getDataCapacity() {
        FirebaseHolder.getUsersSize({
            view.setDataCapacity(it)
        }) {
            view.showError(it)
        }
    }

    override fun queryData() {
        FirebaseHolder.getUsersPage(
            pageSize.toLong(),
            lastVisible,
            updateAfter = { lastVisible = it },
            onError = { view.showError(it) },
            onSuccess = { view.updateFeed(it) }
        )
    }
}