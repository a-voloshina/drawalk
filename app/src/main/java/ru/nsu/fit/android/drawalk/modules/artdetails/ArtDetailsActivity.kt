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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.model.firebase.ArtPolyLine
import ru.nsu.fit.android.drawalk.model.firebase.GpsArt
import ru.nsu.fit.android.drawalk.modules.user.UserActivity
import ru.nsu.fit.android.drawalk.utils.FirebaseHolder
import ru.nsu.fit.android.drawalk.utils.MapUtils
import java.text.SimpleDateFormat
import java.util.*

class ArtDetailsActivity : IArtDetailsActivity(), OnMapReadyCallback {
    companion object {
        const val ART_ID_EXTRA = "ART_ID_EXTRA"
        private const val ART_PADDING_PX = 50
    }

    private lateinit var toolbar: Toolbar
    private lateinit var loadingHolder: LinearLayout
    private lateinit var content: ConstraintLayout

    private lateinit var name: TextView
    private lateinit var authorName: TextView
    private lateinit var created: TextView
    private lateinit var map: GoogleMap

    private var authorId: String? = null
    private var currentArt: List<ArtPolyLine>? = null

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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment)
        if (mapFragment != null) {
            (mapFragment as SupportMapFragment).getMapAsync(this)
        } else {
            throw Exception("null map fragment")
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: throw Exception("got null GoogleMap in onMapReady")
        drawArt()
    }

    override fun updateArtData(art: GpsArt, username: String) {
        authorId = art.authorId
        updateMenuUI()
        this.name.text = art.name
        this.authorName.text = username
        this.created.text = SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.ENGLISH)
            .format(art.created.toDate())
        currentArt = art.parts
        drawArt()
    }

    private fun drawArt() {
        val segments = currentArt?.map(ArtPolyLine::toMapSegment)
        if (this::map.isInitialized && segments != null) {
            map.clear()
            for (segment in segments) {
                map.addPolyline(PolylineOptions()
                    .addAll(segment.coordinates)
                    .color(segment.color)
                    .width(segment.width))
            }
            MapUtils.getBounds(segments)?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(it, ART_PADDING_PX))
            }
        }
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

    private fun updateMenuUI() {
        val currentUserId = FirebaseHolder.AUTH.currentUser?.uid
        val youAreAuthor = currentUserId != null && authorId == currentUserId
        toolbar.menu.setGroupVisible(R.id.authed_art_menu, youAreAuthor)
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