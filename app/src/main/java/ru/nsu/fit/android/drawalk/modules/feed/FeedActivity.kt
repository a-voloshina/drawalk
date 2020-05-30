package ru.nsu.fit.android.drawalk.modules.feed

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.components.autoloading.AutoLoadingRecyclerAdapter
import ru.nsu.fit.android.drawalk.components.autoloading.OnLoadMoreListener
import ru.nsu.fit.android.drawalk.databinding.ActivityListBinding
import ru.nsu.fit.android.drawalk.databinding.ItemArtBinding
import ru.nsu.fit.android.drawalk.model.GpsArt

class FeedActivity : IFeedActivity() {
    private val data = mutableListOf<GpsArt?>()
    private lateinit var adapter: FeedAdapter
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = FeedPresenter(this)

        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(this)
            adapter = FeedAdapter(
                it,
                data
            )
                .also { adapter ->
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

    override fun updateFeed(newData: List<GpsArt>) {
        val index = data.lastIndex
        data.removeAt(index)
        adapter.notifyItemRemoved(index)
        data.addAll(newData)
        adapter.notifyItemRangeInserted(index, newData.size)
        adapter.setLoaded()
    }


    class FeedAdapter(recyclerView: RecyclerView, data: List<GpsArt?>) :
        AutoLoadingRecyclerAdapter<GpsArt, FeedAdapter.FeedViewHolder>(recyclerView, data) {
        class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var binding = ItemArtBinding.bind(view)
        }

        override val dataItemLayoutId = R.layout.item_art

        override fun isItemViewHolder(holder: RecyclerView.ViewHolder) = holder is FeedViewHolder

        override fun provideItemViewHolder(view: View) = FeedViewHolder(view)

        override fun bindDataViewHolder(holder: RecyclerView.ViewHolder, item: GpsArt) {
            val exampleViewHolder = holder as? FeedViewHolder
                ?: throw RuntimeException("Wrong holder type")
            val binding = exampleViewHolder.binding
            binding.txtId.text = item.id
            binding.txtAuthorId.text = item.authorId
        }
    }
}