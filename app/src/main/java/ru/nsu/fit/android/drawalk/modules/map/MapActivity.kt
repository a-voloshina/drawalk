package ru.nsu.fit.android.drawalk.modules.map

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import ru.nsu.fit.android.drawalk.databinding.ActivityMapBinding
import ru.nsu.fit.android.drawalk.modules.animation.ViewAnimation
import ru.nsu.fit.android.drawalk.modules.base.SingleFragmentActivity

class MapActivity : SingleFragmentActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var view: IMapFragment

    private var isFabRotate = false

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
        binding.mapFab.setOnClickListener { view ->
            isFabRotate = ViewAnimation.rotateFab(view, !isFabRotate)
            if (isFabRotate) {
                ViewAnimation.showIn(binding.playPauseFab)
                ViewAnimation.showIn(binding.stopFab)
                ViewAnimation.showIn(binding.cancelFab)
            } else {
                ViewAnimation.showOut(binding.playPauseFab)
                ViewAnimation.showOut(binding.stopFab)
                ViewAnimation.showOut(binding.cancelFab)
            }
        }

        binding.playPauseFab.setOnClickListener {
            view.tryToGetLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LocationPermissionCallback.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                view.handleSuccess()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}