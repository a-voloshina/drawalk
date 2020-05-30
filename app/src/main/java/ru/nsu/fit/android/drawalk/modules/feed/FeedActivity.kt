package ru.nsu.fit.android.drawalk.modules.feed

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.components.autoloading.AutoLoadingRecyclerAdapter
import ru.nsu.fit.android.drawalk.components.autoloading.OnLoadMoreListener

abstract class FeedActivity<T: Any> : IFeedActivity<T>() {
    protected val data = mutableListOf<T?>()
    protected lateinit var adapter: AutoLoadingRecyclerAdapter<T, RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.also {
            it.layoutManager = LinearLayoutManager(this)
            adapter = provideAdapter(
                it,
                data
            ).also { adapter ->
                    adapter.loadMoreListener = object : OnLoadMoreListener {
                        override fun onLoadMore() {
                            presenter.loadMoreData()
                        }
                    }
                }
            it.adapter = adapter
        }

    }

    override fun startLoading() {
        data.add(null)
        adapter.notifyItemInserted(data.lastIndex)
    }

    override fun updateFeed(newData: List<T>) {
        val index = data.lastIndex
        data.removeAt(index)
        adapter.notifyItemRemoved(index)
        data.addAll(newData)
        adapter.notifyItemRangeInserted(index, newData.size)
        adapter.setLoaded()
    }

    protected abstract fun provideAdapter(view: RecyclerView, data: List<T?>): AutoLoadingRecyclerAdapter<T, RecyclerView.ViewHolder>

}