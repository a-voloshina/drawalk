package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ItemArtBinding
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.base.feed.FeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.AutoLoadingRecyclerAdapter

class ArtsFeedFragment: FeedFragment<GpsArtData>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        presenter = ArtsFeedPresenter(this, data)
        presenter.getDataCapacity()
        return view
    }


    inner class FeedAdapter(recyclerView: RecyclerView, data: List<GpsArtData?>) :
        AutoLoadingRecyclerAdapter<GpsArtData, RecyclerView.ViewHolder>(recyclerView, data) {
        inner class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var binding = ItemArtBinding.bind(view)
        }

        override val dataItemLayoutId = R.layout.item_art

        override fun isItemViewHolder(holder: RecyclerView.ViewHolder) = holder is FeedViewHolder

        override fun provideItemViewHolder(view: View) = FeedViewHolder(view)

        override fun bindDataViewHolder(holder: RecyclerView.ViewHolder, item: GpsArtData) {
            val exampleViewHolder = holder as? FeedViewHolder
                ?: throw RuntimeException("Wrong holder type")
            val binding = exampleViewHolder.binding
            binding.txtName.text = item.name
            binding.txtAuthorName.text = item.authorName
            binding.root.setOnClickListener {
                Toast.makeText(activity, "Picked art with ID ${item.id}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun provideAdapter(view: RecyclerView, data: List<GpsArtData?>): AutoLoadingRecyclerAdapter<GpsArtData, RecyclerView.ViewHolder> {
        return FeedAdapter(view, data)
    }

    override fun showError(cause: Throwable) {
        super.showError(cause)
        Toast.makeText(activity, "Cannot load more arts: ${cause.message}", Toast.LENGTH_SHORT).show()
    }
}