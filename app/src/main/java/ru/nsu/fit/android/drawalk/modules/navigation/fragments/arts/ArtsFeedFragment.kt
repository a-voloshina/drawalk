package ru.nsu.fit.android.drawalk.modules.navigation.fragments.arts

import android.content.Intent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ItemArtBinding
import ru.nsu.fit.android.drawalk.model.GpsArtData
import ru.nsu.fit.android.drawalk.modules.artdetails.ArtDetailsActivity
import ru.nsu.fit.android.drawalk.modules.base.feed.FeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.AutoLoadingRecyclerAdapter

open class ArtsFeedFragment: FeedFragment<GpsArtData>() {
    inner class ArtsFeedAdapter(recyclerView: RecyclerView, data: List<GpsArtData?>) :
        AutoLoadingRecyclerAdapter<GpsArtData, RecyclerView.ViewHolder>(recyclerView, data) {
        inner class ArtViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var binding = ItemArtBinding.bind(view)

            fun startLoading() {
                binding.art.visibility = GONE
                binding.progress.visibility = VISIBLE
            }

            fun stopLoading() {
                binding.progress.visibility = GONE
                binding.art.visibility = VISIBLE
            }
        }

        override val dataItemLayoutId = R.layout.item_art

        override fun isItemViewHolder(holder: RecyclerView.ViewHolder) = holder is ArtViewHolder

        override fun provideItemViewHolder(view: View) = ArtViewHolder(view)

        override fun bindDataViewHolder(holder: RecyclerView.ViewHolder, item: GpsArtData) {
            val artHolder = holder as? ArtViewHolder
                ?: throw RuntimeException("Wrong holder type")
            val binding = artHolder.binding
            binding.root.setOnClickListener {
                startActivity(Intent(activity, ArtDetailsActivity::class.java)
                    .putExtra(ArtDetailsActivity.ART_ID_EXTRA, item.id))
            }
            binding.txtName.text = item.name
            binding.txtAuthorName.text = item.authorName
            binding.art.setImageResource(R.mipmap.no_preview)
            item.imageUrl.takeIf { it?.isNotBlank() == true }?.let {
                artHolder.startLoading()
                Picasso.get()
                    .load(item.imageUrl)
                    .error(R.mipmap.no_preview)
                    .into(binding.art, object: Callback{
                        override fun onSuccess() {
                            artHolder.stopLoading()
                        }

                        override fun onError(e: Exception?) {
                            artHolder.stopLoading()
                        }
                    })
            }
        }
    }

    override val noDataText by lazy { getString(R.string.no_arts_text) }

    override fun provideAdapter(view: RecyclerView, data: List<GpsArtData?>) = ArtsFeedAdapter(view, data)

    override fun providePresenter(view: IFeedFragment<GpsArtData>, data: List<GpsArtData?>) = ArtsFeedPresenter(view, data)
}