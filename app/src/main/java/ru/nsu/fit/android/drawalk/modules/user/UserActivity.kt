package ru.nsu.fit.android.drawalk.modules.user

import android.os.Bundle
import android.widget.Toast
import com.squareup.picasso.Picasso
import ru.nsu.fit.android.drawalk.databinding.ActivityUserBinding
import ru.nsu.fit.android.drawalk.model.firebase.UserData

class UserActivity : IUserActivity() {
    companion object {
        const val USER_ID_EXTRA = "USER_ID_EXTRA"
    }

    private lateinit var binding: ActivityUserBinding
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userId = intent.getStringExtra(USER_ID_EXTRA)

        supportFragmentManager
            .beginTransaction()
            .add(binding.arts.id, UserArtsFeedFragment(userId))
            .commit()

        presenter = UserPresenter(userId, this)
    }

    override fun setUserData(userData: UserData) {
        binding.userName.text = userData.name
        userData.imageUrl?.let {
            Picasso.get().load(it).into(binding.avatar)
        }
    }

    override fun showError(cause: Throwable) {
        Toast.makeText(
            this,
            "Cannot load user data: ${cause.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}