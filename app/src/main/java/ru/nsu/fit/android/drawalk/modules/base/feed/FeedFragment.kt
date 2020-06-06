package ru.nsu.fit.android.drawalk.modules.base.feed

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.AutoLoadingRecyclerAdapter
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.OnLoadMoreListener

abstract class FeedFragment<T: Parcelable>(layoutId: Int = R.layout.fragment_feed) : IFeedFragment<T>(layoutId) {
    protected val data = mutableListOf<T?>()
    protected lateinit var adapter: AutoLoadingRecyclerAdapter<T, RecyclerView.ViewHolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    protected open val noDataText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        recyclerView = view!!.findViewById<RecyclerView>(R.id.recycler_view).also {
            it.layoutManager = LinearLayoutManager(activity)
            adapter = provideAdapter(
                it,
                data
            ).also { adapter ->
                adapter.loadMoreListener = object :
                    OnLoadMoreListener {
                    override fun onLoadMore() {
                        presenter.loadMoreData()
                    }
                }
            }
            it.adapter = adapter
        }

        noDataTextView = view.findViewById(R.id.no_data_text)
        noDataText?.let {
            noDataTextView.text = it
        }

        if (savedInstanceState != null) {
            savedInstanceState.getParcelableArrayList<T?>("data")?.let {
                data.clear()
                data.addAll(it)
            }
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"))
            adapter.dataCapacity = savedInstanceState.get("capacity") as? Int?
        }

        presenter = providePresenter(this, data)
        presenter.getDataCapacity()

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::recyclerView.isInitialized && this::adapter.isInitialized) {
            val position = (recyclerView.layoutManager as LinearLayoutManager)
                .findFirstVisibleItemPosition()
            outState.putParcelableArrayList("data", ArrayList(data))
            outState.putInt("position", position)
            adapter.dataCapacity?.let {
                outState.putInt("capacity", it)
            }
        }
    }

    override fun startLoading() {
        data.add(null)
        adapter.notifyItemInserted(data.lastIndex)
    }

    override fun updateFeed(newData: List<T>) {
        endLoading()
        val index = data.size
        data.addAll(newData)
        adapter.notifyItemRangeInserted(index, newData.size)
        adapter.setLoaded()
    }

    override fun showError(cause: Throwable) {
        endLoading()
        Toast.makeText(activity, "Feed error: ${cause.message}", Toast.LENGTH_LONG).show()
    }

    private fun endLoading() {
        val index = data.lastIndex
        if (index >= 0) {
            data.removeAt(index)
            adapter.notifyItemRemoved(index)
        }
    }

    protected abstract fun provideAdapter(view: RecyclerView, data: List<T?>): AutoLoadingRecyclerAdapter<T, RecyclerView.ViewHolder>
    protected abstract fun providePresenter(view: IFeedFragment<T>, data: List<T?>): IFeedPresenter

    override fun setDataCapacity(cap: Int?) {
        if (cap == 0) {
            recyclerView.visibility = View.GONE
            noDataTextView.visibility = View.VISIBLE
        }
        adapter.dataCapacity = cap
    }
}