package ru.nsu.fit.android.drawalk.modules.map

import com.google.android.gms.maps.model.LatLng
import ru.nsu.fit.android.drawalk.modules.base.IViewFragment
import ru.nsu.fit.android.drawalk.utils.permission.PermissionResultHandler

abstract class IMapFragment(layoutId: Int) : IViewFragment<IMapPresenter>(layoutId), PermissionResultHandler,
    OnGPSCheckingListener {

    abstract fun addMarker(position: LatLng, title: String)
    abstract fun moveCamera(position: LatLng)
    abstract fun moveAndZoomCamera(position: LatLng, zoom: Float)
    abstract fun tryToGetLocation()
    abstract fun isDrawingModeOn() : Boolean
    abstract fun turnDrawingModeOn()
    abstract fun turnDrawingModeOff()
    abstract fun cancelDrawing()
    abstract fun stopDrawing()
    abstract fun checkGPSisOn(): Boolean
    abstract fun isLocationAvailable() : Boolean
}