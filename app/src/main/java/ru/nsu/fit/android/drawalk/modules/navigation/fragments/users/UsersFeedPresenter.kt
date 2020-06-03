package ru.nsu.fit.android.drawalk.modules.navigation.fragments.users

import com.google.firebase.firestore.QuerySnapshot
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
        FirebaseHolder.USERS
            .get().addOnFailureListener {
                view.showError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    view.setDataCapacity(it.result!!.size())
                } else {
                    view.showError(RuntimeException("Null users response"))
                }
            }
    }

    override fun queryData() {
        var query = FirebaseHolder.USERS
            .orderBy("id")
        lastVisible?.let {
            query = query.startAfter(it)
        }
        query.limit(pageSize.toLong())
            .get().addOnFailureListener {
                endWithError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    try {
                        handleUsersResponse(it.result!!)
                    } catch (ex: Throwable) {
                        endWithError(ex)
                    }
                } else {
                    endWithError(RuntimeException("Null arts response"))
                }
            }
    }

    private fun handleUsersResponse(response: QuerySnapshot) {
        response.documents.lastOrNull()?.let {
            lastVisible = it
        }
        endWithData(response.documents.map {
            it.toObject(UserData::class.java)
                ?: throw RuntimeException("Cannot cast document to user: $it")
        })
    }
}