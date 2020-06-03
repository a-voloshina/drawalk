package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ItemArtBinding
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.base.feed.FeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.AutoLoadingRecyclerAdapter

class ArtsFeedFragment: FeedFragment<GpsArtData>() {
    inner class ArtsFeedAdapter(recyclerView: RecyclerView, data: List<GpsArtData?>) :
        AutoLoadingRecyclerAdapter<GpsArtData, RecyclerView.ViewHolder>(recyclerView, data) {
        inner class ArtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var binding = ItemArtBinding.bind(view)
        }

        override val dataItemLayoutId = R.layout.item_art

        override fun isItemViewHolder(holder: RecyclerView.ViewHolder) = holder is ArtViewHolder

        override fun provideItemViewHolder(view: View) = ArtViewHolder(view)

        override fun bindDataViewHolder(holder: RecyclerView.ViewHolder, item: GpsArtData) {
            val artHolder = holder as? ArtViewHolder
                ?: throw RuntimeException("Wrong holder type")
            val binding = artHolder.binding
            binding.txtName.text = item.name
            binding.txtAuthorName.text = item.authorName
            binding.root.setOnClickListener {
                Toast.makeText(activity, "Picked art with ID ${item.id}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun provideAdapter(view: RecyclerView, data: List<GpsArtData?>) = ArtsFeedAdapter(view, data)

    override fun providePresenter(view: IFeedFragment<GpsArtData>, data: List<GpsArtData?>) = ArtsFeedPresenter(view, data)
}