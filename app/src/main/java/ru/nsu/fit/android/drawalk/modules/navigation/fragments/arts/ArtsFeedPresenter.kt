package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.modules.base.feed.FirebaseFeedPresenter
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class ArtsFeedPresenter(
    view: IFeedFragment<GpsArtData>,
    data: List<GpsArtData?>,
    pageSize: Int = 10
) : FirebaseFeedPresenter<GpsArtData>(view, data, pageSize) {
    override fun getDataCapacity() {
        FirebaseHolder.ARTS
            .get().addOnFailureListener {
                view.showError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    view.setDataCapacity(it.result!!.size())
                } else {
                    view.showError(RuntimeException("Null arts response"))
                }
            }
    }

    override fun queryData() {
        var query = FirebaseHolder.ARTS
            .orderBy("created", Query.Direction.DESCENDING)
        lastVisible?.let {
            query = query.startAfter(it)
        }
        query.limit(pageSize.toLong())
            .get().addOnFailureListener {
                endWithError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    try {
                        handleArtsResponse(it.result!!)
                    } catch (ex: Throwable) {
                        endWithError(ex)
                    }
                } else {
                    endWithError(RuntimeException("Null arts response"))
                }
            }
    }

    private fun handleArtsResponse(response: QuerySnapshot) {
        val userIds = mutableListOf<String>()
        val arts = mutableListOf<GpsArt>()

        response.documents.lastOrNull()?.let {
            lastVisible = it
        }

        for (document in response.documents) {
            val art = document.toObject(GpsArt::class.java)
                ?: throw RuntimeException("Cannot cast document to art: $document")
            arts.add(art)
            userIds.add(art.authorId)
        }

        if (arts.isEmpty()) {
            endWithData(listOf())
            return
        }

        FirebaseHolder.USERS
            .whereIn("id", userIds)
            .get().addOnFailureListener {
                endWithError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    handleUsersResponse(it.result!!, arts)
                } else {
                    endWithError(RuntimeException("Null users response"))
                }
            }
    }

    private fun handleUsersResponse(response: QuerySnapshot, arts: List<GpsArt>) {
        val out = mutableListOf<GpsArtData>()

        val users = response.documents.map {
            it.toObject(UserData::class.java)
                ?: throw RuntimeException("Cannot cast document to user: $it")
        }

        for (art in arts) {
            val authorName = users
                .filter { it.id == art.authorId }
                .map { it.name }
                .firstOrNull() ?: "NOT FOUND"
            out.add(GpsArtData(art, authorName))
        }
        endWithData(out)
    }
}