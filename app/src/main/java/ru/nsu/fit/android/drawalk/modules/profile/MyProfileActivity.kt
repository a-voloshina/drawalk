package ru.nsu.fit.android.drawalk.modules.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ActivityMyProfileBinding
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder

class MyProfileActivity : IMyProfileActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var loadingHolder: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingHolder = findViewById(R.id.loading_holder)
        presenter = MyProfilePresenter(this)
        binding.submit.setOnClickListener {
            startLoading(getString(R.string.loading_updating_username))
            presenter.updateName(binding.name.text.toString())
        }
        binding.cancel.setOnClickListener { updateUserData() }
        binding.delete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_account_dialog_title))
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.sure)) { _, _ ->
                    startLoading(getString(R.string.loading_deleting_app_data))
                    presenter.deleteAccount()

                }.setNegativeButton(getString(R.string.not_sure)) { dialog, _ ->
                    dialog.cancel()
                }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserData()
    }

    override fun updateUserData() {
        FirebaseHolder.AUTH.currentUser?.also {
            setUserData(it)
        } ?: showError(RuntimeException("You are not authenticated"))
    }

    override fun onUserInfoDeleted() {
        findViewById<TextView>(R.id.progress_text).text = getString(R.string.loading_deleting_auth)
    }

    override fun closeAfterDeletion() {
        stopLoading()
        finish()
    }

    override fun showError(cause: Throwable) {
        stopLoading()
        Toast.makeText(this, "Error: ${cause.message}", Toast.LENGTH_LONG).show()
    }

    private fun setUserData(user: FirebaseUser) {
        binding.email.text = user.email
        binding.name.setText(user.displayName)
        Picasso.get().load(user.photoUrl).into(binding.avatar)
    }

    private fun startLoading(text: String? = null) {
        text?.let {
            findViewById<TextView>(R.id.progress_text).text = it
        }
        loadingHolder.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
    }

    private fun stopLoading() {
        loadingHolder.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
    }
}