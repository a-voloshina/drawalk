package ru.nsu.fit.android.drawalk.modules.map

import com.google.android.gms.maps.model.LatLng
import ru.nsu.fit.android.drawalk.modules.base.IViewFragment
import ru.nsu.fit.android.drawalk.modules.permission.PermissionResultHandler

abstract class IMapFragment : IViewFragment<IMapPresenter>(), PermissionResultHandler {
    abstract fun addMarker(position : LatLng, title: String)
    abstract fun moveCamera(position : LatLng)
    abstract fun moveAndZoomCamera(position : LatLng, zoom: Float)
    abstract fun tryToGetLocation()
    abstract fun turnDrawingModeOn()
    abstract fun turnDrawingModeOff()
    abstract fun cancelDrawing()
}