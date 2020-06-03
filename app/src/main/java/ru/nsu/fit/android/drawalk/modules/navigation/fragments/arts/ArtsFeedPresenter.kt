package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedPresenter
import ru.nsu.fit.android.drawalk.utils.FirestoreHolder

class ArtsFeedPresenter(
    private val view: IFeedFragment<GpsArtData>,
    private val data: List<GpsArtData?>,
    private val pageSize: Int = 10
) : IFeedPresenter {
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false

    companion object {
        private const val PLT = "PLT"
    }

    override fun getDataCapacity() {
        FirestoreHolder.ARTS
            .get().addOnFailureListener {
                view.showError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    Log.d(PLT, "Got all arts, size: ${it.result!!.size()}")
                    view.setDataCapacity(it.result!!.size())
                } else {
                    Log.d(PLT, "Error getting all arts")
                    view.showError(RuntimeException("Null arts response"))
                }
            }
        Log.d(PLT, "Started getting all arts")
//        FirestoreHolder.addStubArt()
    }

    override fun loadMoreData() {
        Log.d(PLT, "Load more data")
        load()
    }

    override fun start() {
        Log.d(PLT, "Start")
        if (data.isEmpty()) {
            load()
        }
    }

    private fun load() {
        if (loading) {
            return
        }
        loading = true

        view.startLoading()

        Log.d(PLT, "Load $pageSize from ${data.lastIndex}")
        var query = FirestoreHolder.ARTS
            .orderBy("created", Query.Direction.DESCENDING)
        lastVisible?.let {
            Log.d(PLT, "Adding start after ${it.data}")
            query = query.startAfter(it)
        }
        query.limit(pageSize.toLong())
            .get().addOnFailureListener {
                endWithError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    try {
                        Log.d(PLT, "Got page arts: ${it.result!!.documents.map { d -> d.data }}")
                        handleArtsResponse(it.result!!)
                    } catch (ex: Throwable) {
                        endWithError(ex)
                        Log.d(PLT, "Error getting page arts")
                    }
                } else {
                    Log.d(PLT, "Got null page arts")
                    endWithError(RuntimeException("Null arts response"))
                }
            }
        Log.d(PLT, "Started getting page arts")
    }

    private fun handleArtsResponse(response: QuerySnapshot) {
        val userIds = mutableListOf<String>()
        val arts = mutableListOf<GpsArt>()

        response.documents.lastOrNull()?.let {
            Log.d(PLT, "Setting last to ${it.data}")
            lastVisible = it
        }

        for (document in response.documents) {
            val art = document.toObject(GpsArt::class.java)
                ?: throw RuntimeException("Cannot cast document to art: $document")
            arts.add(art)
            userIds.add(art.authorId)
        }

        if (arts.isEmpty()) {
            Log.d(PLT, "Arts are empty")
            endWithData(listOf())
            return
        }

        FirestoreHolder.USERS
            .whereIn("id", userIds)
            .get().addOnFailureListener {
                Log.d(PLT, "Error getting users")
                endWithError(it)
            }.addOnCompleteListener {
                if (it.result != null) {
                    Log.d(PLT, "Got users: ${it.result!!.documents.map { d -> d.data }}")
                    handleUsersResponse(it.result!!, arts)
                } else {
                    Log.d(PLT, "Got null users")
                    endWithError(RuntimeException("Null users response"))
                }
            }
        Log.d(PLT, "Started getting users")
    }

    private fun handleUsersResponse(response: QuerySnapshot, arts: List<GpsArt>) {
        val users = mutableListOf<UserData>()
        val out = mutableListOf<GpsArtData>()

        for (document in response.documents) {
            val user = document.toObject(UserData::class.java)
                ?: throw RuntimeException("Cannot cast document to user: $document")
            users.add(user)
        }

        for (art in arts) {
            val authorName = users
                .filter { it.id == art.authorId }
                .map { it.name }
                .firstOrNull() ?: "NOT FOUND"

            out.add(
                GpsArtData(
                    art,
                    authorName,
                    ""
                )
            )
        }
        endWithData(out)
    }

    private fun endWithError(e: Throwable) {
        loading = false
        view.showError(e)
    }

    private fun endWithData(data: List<GpsArtData>) {
        loading = false
        view.updateFeed(data)
    }
}