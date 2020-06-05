package ru.nsu.fit.android.drawalk.modules.navigation.fragments.users

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ItemUserBinding
import ru.nsu.fit.android.drawalk.model.firebase.UserData
import ru.nsu.fit.android.drawalk.modules.base.feed.FeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.IFeedFragment
import ru.nsu.fit.android.drawalk.modules.base.feed.adapter.AutoLoadingRecyclerAdapter
import ru.nsu.fit.android.drawalk.modules.user.UserActivity

class UsersFeedFragment: FeedFragment<UserData>() {
    inner class UsersFeedAdapter(recyclerView: RecyclerView, data: List<UserData?>) :
    AutoLoadingRecyclerAdapter<UserData, RecyclerView.ViewHolder>(recyclerView, data) {
        inner class UserViewHolder(view: View): RecyclerView.ViewHolder(view) {
            var binding = ItemUserBinding.bind(view)
        }

        override val dataItemLayoutId = R.layout.item_user

        override fun isItemViewHolder(holder: RecyclerView.ViewHolder) = holder is UserViewHolder

        override fun provideItemViewHolder(view: View) = UserViewHolder(view)

        override fun bindDataViewHolder(holder: RecyclerView.ViewHolder, item: UserData) {
            val userHolder = holder as? UserViewHolder
                ?: throw RuntimeException("Wrong holder type")
            val binding = userHolder.binding
            binding.txtId.text = item.id
            binding.txtName.text = item.name
            binding.root.setOnClickListener {
                startActivity(Intent(activity, UserActivity::class.java)
                    .putExtra(UserActivity.USER_ID_EXTRA, item.id))
            }
        }
    }

    override val noDataText by lazy { getString(R.string.no_users_text) }

    override fun provideAdapter(view: RecyclerView, data: List<UserData?>) = UsersFeedAdapter(view, data)

    override fun providePresenter(view: IFeedFragment<UserData>, data: List<UserData?>) = UsersFeedPresenter(view, data)
}