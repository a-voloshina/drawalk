package ru.nsu.fit.android.drawalk.modules.arts

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.components.autoloading.AutoLoadingRecyclerAdapter
import ru.nsu.fit.android.drawalk.databinding.ItemArtBinding
import ru.nsu.fit.android.drawalk.model.GpsArt
import ru.nsu.fit.android.drawalk.modules.feed.FeedActivity

class ArtsFeedActivity: FeedActivity<GpsArt>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ArtsFeedPresenter(this)
        //TODO: add fab
    }


    class FeedAdapter(recyclerView: RecyclerView, data: List<GpsArt?>) :
        AutoLoadingRecyclerAdapter<GpsArt, RecyclerView.ViewHolder>(recyclerView, data) {
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

    override fun provideAdapter(view: RecyclerView, data: List<GpsArt?>): AutoLoadingRecyclerAdapter<GpsArt, RecyclerView.ViewHolder> {
        return FeedAdapter(view, data)
    }

    override fun showError(cause: Throwable) {
        Toast.makeText(this, "Cannot load more arts", Toast.LENGTH_SHORT).show()
    }
}