package ru.nsu.fit.android.drawalk.modules.map

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.databinding.ActivityMapBinding
import ru.nsu.fit.android.drawalk.modules.base.activity.SingleFragmentActivity
import ru.nsu.fit.android.drawalk.modules.base.loading.AsynchronousWorkActivity

class MapActivity : SingleFragmentActivity(),
    AsynchronousWorkActivity {
    private lateinit var binding: ActivityMapBinding
    private lateinit var view: IMapFragment

    private var isFabRotate = false
    private val playColor by lazy {
        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_20))
    }
    private val pauseColor by lazy {
        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
    }

    override fun createFragment(): Fragment = MapFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        view = fragment as IMapFragment
        val presenter = MapPresenter(view)
        view.presenter = presenter

        ViewAnimation.init(binding.playPauseFab)
        ViewAnimation.init(binding.stopFab)
        ViewAnimation.init(binding.cancelFab)
        binding.mapFab.setOnClickListener { fabView ->
            if (view.checkGPSisOn()) {
                isFabRotate = ViewAnimation.rotateFab(fabView, !isFabRotate)
                if (isFabRotate) {
                    ViewAnimation.showIn(binding.playPauseFab)
                    ViewAnimation.showIn(binding.stopFab)
                    ViewAnimation.showIn(binding.cancelFab)
                } else {
                    ViewAnimation.showOut(binding.playPauseFab)
                    ViewAnimation.showOut(binding.stopFab)
                    ViewAnimation.showOut(binding.cancelFab)
                }
            } else {
                Toast.makeText(
                    this,
                    "Для визуализации маршрута необходимо включить GPS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setPlayPauseButtonColor()

        binding.playPauseFab.setOnClickListener {
            if (view.isDrawingModeOn()) {
                view.turnDrawingModeOff()
                binding.playPauseFab.setImageResource(R.drawable.ic_play)
                binding.playPauseFab.backgroundTintList = playColor
            } else {
                view.turnDrawingModeOn()
                binding.playPauseFab.setImageResource(R.drawable.ic_pause)
                binding.playPauseFab.backgroundTintList = pauseColor
            }
        }
        binding.cancelFab.setOnClickListener {
            view.cancelDrawing()
        }
        binding.stopFab.setOnClickListener {
            view.stopDrawing()
        }
        binding.stopFab.setOnLongClickListener {

            return@setOnLongClickListener true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LocationPermissionCallback.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //view.handleSuccessfullyGetPermission()
                startLocationService()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startLocationService() {
        startService(Intent(this, LocationService::class.java))  //TODO: или поместить в сервис или выпилить его
    }

    override fun startProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun stopProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun setPlayPauseButtonColor(){
        Toast.makeText(this, "drawing mode = ${view.isDrawingModeOn()}", Toast.LENGTH_SHORT).show()
        if (view.isDrawingModeOn()) {
            binding.playPauseFab.setImageResource(R.drawable.ic_pause)
            binding.playPauseFab.backgroundTintList = pauseColor
        } else {
            binding.playPauseFab.setImageResource(R.drawable.ic_play)
            binding.playPauseFab.backgroundTintList = playColor
        }
    }
}
