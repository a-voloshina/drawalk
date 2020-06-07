package ru.nsu.fit.android.drawalk.modules.map

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.Context.LOCATION_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import ru.nsu.fit.android.drawalk.R
import ru.nsu.fit.android.drawalk.model.MapSegment
import ru.nsu.fit.android.drawalk.modules.base.loading.AsynchronousWorkActivity
import java.io.File
import java.io.FileOutputStream

class MapFragment : IMapFragment(R.layout.fragment_map), OnMapReadyCallback,
    MapDrawingSettingsListener {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 128
        const val DIALOG_TAG = "show dialog in MapFragment"
        const val SAVE_DRAWING_MODE_MESSAGE = "save drawing mode"
        const val SAVE_COLOR_MESSAGE = "save current line color"
        const val SAVE_WIDTH_MESSAGE = "save current line width"
        const val SAVE_POINTS_MESSAGE = "save points"
        const val SAVE_ZOOM_MESSAGE = "save map zoom"
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var noGPSMessage: LinearLayout
    private var currentLineColor: Int = Color.RED
    private var currentLineWidth: Int = 10

    private var points: ArrayList<MapSegment> = ArrayList()
    private var segment = MapSegment(
        color = currentLineColor,
        width = currentLineWidth.toFloat()
    )
    private val myActivity: Activity by lazy { activity as Activity }
    private val locationManager: LocationManager by lazy {
        myActivity.getSystemService(LOCATION_SERVICE) as LocationManager
    }
    private val asynchronousWorkActivity by lazy { activity as AsynchronousWorkActivity }
    private var isDrawingModeOn = false
    private var mapZoomSaved = 15f

    private val gpsSwitchStateReceiver = object : BroadcastReceiver() {
        private var firstTimeChange = true
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled || isNetworkEnabled) {
                    if (firstTimeChange) {
                        firstTimeChange = false
                        moveToCurrentLocation()
                        showToast("GPS turned on first time")
                    }
                    noGPSMessage.visibility = View.GONE
                } else {
                    noGPSMessage.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMap()
        view.findViewById<TextView>(R.id.open_gps_settings_button)
            .setOnClickListener {
                openGPSSettingsScreen()
            }
        noGPSMessage = view.findViewById(R.id.no_gps_message)
        view.findViewById<Toolbar>(R.id.map_toolbar).apply {
            setTitle(R.string.create_art)
            setTitleTextColor(Color.WHITE)
            inflateMenu(R.menu.menu_map_fragment)
            setOnMenuItemClickListener {
                openMapDrawingSettingsDialog()
                return@setOnMenuItemClickListener true
            }
        }
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(Intent.ACTION_PROVIDER_CHANGED)
        }
        myActivity.registerReceiver(gpsSwitchStateReceiver, filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myActivity.unregisterReceiver(gpsSwitchStateReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapZoomSaved = map.cameraPosition.zoom
        points.add(segment)
        outState.putBoolean(SAVE_DRAWING_MODE_MESSAGE, isDrawingModeOn)
        outState.putInt(SAVE_COLOR_MESSAGE, currentLineColor)
        outState.putInt(SAVE_WIDTH_MESSAGE, currentLineWidth)
        outState.putParcelableArrayList(SAVE_POINTS_MESSAGE, points)
        outState.putFloat(SAVE_ZOOM_MESSAGE, mapZoomSaved)
    }

    private fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isDrawingModeOn = savedInstanceState.getBoolean(SAVE_DRAWING_MODE_MESSAGE)
        currentLineColor = savedInstanceState.getInt(SAVE_COLOR_MESSAGE)
        currentLineWidth = savedInstanceState.getInt(SAVE_WIDTH_MESSAGE)
        points =
            savedInstanceState.getParcelableArrayList<MapSegment>(SAVE_POINTS_MESSAGE) as ArrayList
        mapZoomSaved = savedInstanceState.getFloat(SAVE_ZOOM_MESSAGE)
    }

    override fun showError(cause: Throwable) {
        showToast(cause.message)
    }

    private fun getMap() {
        asynchronousWorkActivity.startProgressBar()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment)
        if (mapFragment != null) {
            (mapFragment as SupportMapFragment).getMapAsync(this)
        } else {
            throw Exception("null map fragment")
        }
    }

    override fun tryToGetLocation() {
        val explanationMessage = activity?.getString(R.string.explanation_dialog_message)
        LocationPermissionCallback(activity as Activity, this)
            .requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                explanationMessage ?: ""
            )
    }

    override fun isDrawingModeOn(): Boolean {
        return isDrawingModeOn
    }

    override fun turnDrawingModeOn() {
        isDrawingModeOn = true
    }

    override fun turnDrawingModeOff() {
        isDrawingModeOn = false
    }

    override fun cancelDrawing() {
        AlertDialog.Builder(context)
            .setPositiveButton(R.string.accept) { _, _ ->
                points.clear()
                segment.coordinates.clear()
                map.clear()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .setMessage(R.string.erase_art_description)
            .show()
    }

    override fun stopDrawing() {
        finish()
        takeMapSnapshot()
    }

    override fun checkGPSisOn(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: throw Exception("got null GoogleMap in onMapReady")
        asynchronousWorkActivity.stopProgressBar()
        val explanationMessage = activity?.getString(R.string.explanation_dialog_message)
        LocationPermissionCallback(activity as Activity, this)
            .requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                explanationMessage ?: ""
            )
        if (points.isNotEmpty()) {
            drawAll()
        }
    }

    override fun addMarker(position: LatLng, title: String) {
        map.addMarker(MarkerOptions().position(position).title(title))
    }

    override fun moveCamera(position: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    override fun moveAndZoomCamera(position: LatLng, zoom: Float) {
        val cameraPosition = CameraPosition.Builder().target(position).zoom(zoom).build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun showToast(text: String?) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    private fun showSadMessage() {
        showToast("Can't open map without permission :(")
    }

    private fun showSadGPSMessage() {
        showToast("Не могу определить местоположение без подключения к GPS :(")
    }

    override fun handleSuccessfullyGetPermission() {
        map.isMyLocationEnabled = true
        map.setOnMyLocationClickListener { location ->
            showToast("Current location: $location")
        }
        val locationRequest = createLocationRequest()
        if (locationRequest != null) {
            startLocationUpdates(locationRequest)
        }
    }

    override fun handleCantGetPermission() {
        showSadMessage()
    }

    override fun onGPSCheckingSuccess() {
        moveToCurrentLocation()
    }

    override fun onGPSCheckingFailure() {
        showSadGPSMessage()
    }

    override fun onMapDrawingSettingsChanged(color: Int, width: Int) {
        currentLineColor = color
        currentLineWidth = width
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 5000    //location updates rate in milliseconds
            fastestInterval = 2000  //location updates fastest rate in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val client: SettingsClient = LocationServices.getSettingsClient(myActivity)
        client.checkLocationSettings(locationSettingsRequest)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(myActivity).apply {
                requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        onLocationChanged(locationResult.lastLocation)
                    }
                }, Looper.myLooper())
            }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledDialogToUser()
            noGPSMessage.visibility = View.VISIBLE
        } else {
            moveToCurrentLocation()
            noGPSMessage.visibility = View.GONE
        }
    }

    fun onLocationChanged(location: Location) {
        if (isDrawingModeOn) {
            addPoint(location)
            drawSegment(segment)
        }
    }

    private fun addPoint(location: Location) {
        val point = LatLng(location.latitude, location.longitude)
        if (currentLineColor == segment.color && currentLineWidth.toFloat() == segment.width) {
            segment.coordinates.add(point)
        } else {
            points.add(segment)
            val lastPoint = if (segment.coordinates.isNotEmpty()) {
                segment.coordinates[segment.coordinates.lastIndex]
            } else {
                null
            }
            segment = MapSegment(
                color = currentLineColor,
                width = currentLineWidth.toFloat()
            ).apply {
                if (lastPoint != null) {
                    coordinates.add(lastPoint)
                }
                coordinates.add(point)
            }
        }
    }

    private fun drawSegment(segment: MapSegment) {
        val coordsCount = segment.coordinates.size
        if (coordsCount > 1) {
            map.addPolyline(
                PolylineOptions()
                    .addAll(segment.coordinates)
                    .color(segment.color)
                    .width(segment.width)
            )
        }
    }

    private fun drawAll() {
        for (segment in points) {
            drawSegment(segment)
        }
    }

    private fun moveToCurrentLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    moveAndZoomCamera(LatLng(location.latitude, location.longitude), mapZoomSaved)
                } else {
                    showToast("receive null location")
                }
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(
                            myActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    private fun showGPSDisabledDialogToUser() {
        AlertDialog.Builder(myActivity)
            .setMessage(getString(R.string.gps_explanation_dialog_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                openGPSSettingsScreen()
            }
            .setNegativeButton(R.string.no) { _, _ ->
                showSadGPSMessage()
            }
            .create()
            .show()
    }

    private fun openGPSSettingsScreen() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun takeMapSnapshot() {
        asynchronousWorkActivity.startProgressBar()
        map.snapshot { bitmap ->
            asynchronousWorkActivity.stopProgressBar()
            openMapSnapshotDialog(bitmap)
        }
    }

    private fun saveToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(myActivity.applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "UniqueFileName" + ".jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return Uri.parse(file.absolutePath)
    }

    private fun openMapSnapshotDialog(imageBitmap: Bitmap) {
        MapSnapshotDialog.newInstance(imageBitmap, points)
            .show(childFragmentManager, DIALOG_TAG)
    }

    private fun openMapDrawingSettingsDialog() {
        (MapDrawingSettingsDialog.newInstance(
            currentLineColor,
            currentLineWidth
        ) as MapDrawingSettingsDialog)
            .addListener(this)
            .show(childFragmentManager, DIALOG_TAG)
    }

    private fun finish() {
        points.add(segment)
        map.isMyLocationEnabled = false
    }
}
