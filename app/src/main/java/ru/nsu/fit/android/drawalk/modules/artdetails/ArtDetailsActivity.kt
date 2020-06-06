package ru.nsu.fit.android.drawalk.modules.artdetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.modules.user.UserActivity
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder
import java.text.SimpleDateFormat
import java.util.*

class ArtDetailsActivity : IArtDetailsActivity() {
    companion object {
        const val ART_ID_EXTRA = "ART_ID_EXTRA"
    }

    private lateinit var toolbar: Toolbar
    private lateinit var loadingHolder: LinearLayout
    private lateinit var content: ConstraintLayout

    private lateinit var name: TextView
    private lateinit var authorName: TextView
    private lateinit var created: TextView

    private var authorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art)

        loadingHolder = findViewById(R.id.loading_holder)
        content = findViewById(R.id.content)

        name = findViewById(R.id.txt_art_name)
        created = findViewById(R.id.txt_created)
        authorName = findViewById(R.id.txt_author_name)
        authorName.setOnClickListener {
            if (authorId != null) {
                startActivity(
                    Intent(this, UserActivity::class.java)
                        .putExtra(UserActivity.USER_ID_EXTRA, authorId)
                )
            } else {
                showError(RuntimeException("Author not found"))
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val artId = intent.getStringExtra(ART_ID_EXTRA)
        presenter = ArtDetailsPresenter(this, artId)
    }

    override fun updateArtData(art: GpsArt, username: String) {
        authorId = art.authorId
        this.name.text = art.name
        this.authorName.text = username
        this.created.text = SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.ENGLISH)
            .format(art.created.toDate())
        //TODO: initialize map with data.parts
    }

    override fun updateArtName(name: String) {
        stopLoading()
        this.name.text = name
    }

    override fun closeOnArtDeleted() {
        stopLoading()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.art, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            val currentUserId = FirebaseHolder.AUTH.currentUser?.uid
            val youAreAuthor = currentUserId != null && authorId == currentUserId
            it.setGroupVisible(R.id.authed_art_menu, youAreAuthor)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_rename_art -> {
            val builder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.rename_art_dialog_title))
            val view = layoutInflater.inflate(R.layout.content_rename, null)
            val artName = view.findViewById<EditText>(R.id.new_art_name)
            builder
                .setView(view)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    startLoading(getString(R.string.rename_art_loading))
                    presenter.renameArt(artName.text.toString())

                }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                }.show()
            true
        }
        R.id.action_delete_art -> {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.art_delete_dialog_title))
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.sure)) { _, _ ->
                    startLoading(getString(R.string.delete_art_loading))
                    presenter.deleteArt()

                }.setNegativeButton(getString(R.string.not_sure)) { dialog, _ ->
                    dialog.cancel()
                }.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun showError(cause: Throwable) {
        stopLoading()
        Toast.makeText(this, "Error: ${cause.message}", Toast.LENGTH_LONG).show()
    }

    private fun startLoading(text: String? = null) {
        text?.let {
            findViewById<TextView>(R.id.progress_text).text = it
        }
        loadingHolder.visibility = View.VISIBLE
        content.visibility = View.GONE
    }

    private fun stopLoading() {
        loadingHolder.visibility = View.GONE
        content.visibility = View.VISIBLE
    }
}