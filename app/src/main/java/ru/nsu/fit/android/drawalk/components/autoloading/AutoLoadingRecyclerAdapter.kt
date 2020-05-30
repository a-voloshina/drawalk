package ru.nsu.fit.android.drawalk.components.autoloading

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.nsu.fit.android.drawalk.R

abstract class AutoLoadingRecyclerAdapter<T : Any, VH : ViewHolder>(
    recyclerView: RecyclerView,
    private val data: List<T?>,
    startWthLoading: Boolean = true
) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private class LoadingViewHolder(view: View) : ViewHolder(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.progress)
    }

    private val adapter by lazy { this }

    lateinit var loadMoreListener: OnLoadMoreListener
    var visibleThreshold = 5

    private var isLoading = startWthLoading
    private var lastVisibleItem = 0
    private var totalItemCount = 0

    protected abstract val dataItemLayoutId: Int

    protected abstract fun isItemViewHolder(holder: ViewHolder): Boolean
    protected abstract fun provideItemViewHolder(view: View): VH
    protected abstract fun bindDataViewHolder(holder: ViewHolder, item: T)

    init {
        val linearLayoutManager = recyclerView.layoutManager as? LinearLayoutManager
            ?: throw RuntimeException("No linearLayoutManager")
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (!adapter::loadMoreListener.isInitialized) {
                        throw RuntimeException("No load listener")
                    }
                    loadMoreListener.onLoadMore()
                    isLoading = true
                }
            }
        })
    }

    fun setLoaded() {
        isLoading = false
    }

    override fun getItemViewType(position: Int): Int {
        Log.d("AUTO_LOADING_ADAPTER", "Type: Position is $position, data size is ${data.size}")
        return if (data[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(dataItemLayoutId, parent, false)
                provideItemViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw RuntimeException("Incorrect view type")
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("AUTO_LOADING_ADAPTER", "Bind: Position is $position, data size is ${data.size}")
        if (isItemViewHolder(holder)) {
            val item = data[position] ?: throw RuntimeException("Unexpected error: data is null")
            bindDataViewHolder(holder, item)
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }
}