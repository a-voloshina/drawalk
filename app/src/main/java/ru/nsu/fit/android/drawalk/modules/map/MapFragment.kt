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
import android.provider.MediaStore
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
import ru.nsu.fit.android.drawalk.modules.base.AsynchronousWorkActivity
import java.io.File
import java.io.FileOutputStream

class MapFragment : IMapFragment(), OnMapReadyCallback, MapDrawingSettingsListener {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 128
        const val DIALOG_TAG = "show dialog in MapFragment"
    }

    private lateinit var map: GoogleMap //
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var noGPSMessage: LinearLayout
    private var currentLineColor: Int = Color.RED
    private var currentLineWidth: Int = 10

    private val pointsList: MutableList<MapPoint> = ArrayList()
    private val points: MutableList<LatLng> = ArrayList()
    private val myActivity: Activity by lazy { activity as Activity }
    private val locationManager: LocationManager by lazy {
        myActivity.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private var options: PolylineOptions = makeOptions()
    private var isDrawingModeOn = false

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
        return if (view == null) {
            inflater.inflate(R.layout.fragment_map, container, false)
        } else {
            view
        }
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
            setTitle(R.string.app_name)
            setTitleTextColor(Color.WHITE)
            inflateMenu(R.menu.menu_map_fragment)
            setOnMenuItemClickListener {
                openMapDrawingSettingsDialog()
                return@setOnMenuItemClickListener true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(Intent.ACTION_PROVIDER_CHANGED)
        }
        myActivity.registerReceiver(gpsSwitchStateReceiver, filter)
    }

    private fun getMap() {
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

    override fun turnDrawingModeOn() {
        isDrawingModeOn = true
    }

    override fun turnDrawingModeOff() {
        isDrawingModeOn = false
    }

    override fun cancelDrawing() {
        map.clear()
    }

    override fun stopDrawing() {
        //points.clear()
        //turnDrawingModeOff()            //TODO: bind with ui
        finish()
        takeMapSnapshot()
    }

    override fun checkGPSisOn(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: throw Exception("got null GoogleMap in onMapReady")
        val explanationMessage = activity?.getString(R.string.explanation_dialog_message)
        LocationPermissionCallback(activity as Activity, this)
            .requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                explanationMessage ?: ""
            )
        //setTestPointsSet()
        map.setOnMapClickListener { location ->
            //showToast("Location: ${location.latitude}, ${location.longitude}")

            val mapPoint = MapPoint(
                LatLng(location.latitude, location.longitude),
                currentLineColor,
                currentLineWidth.toFloat()
            )
            pointsList.add(mapPoint)
            redrawLine()
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

    private fun showToast(text: String) {
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
        if (locationRequest == null) {
            showToast("get null locationRequest") //TODO: dialog?
        } else {
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
        //options = makeOptions()
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000    //location updates rate in milliseconds
            fastestInterval = 5000  //location updates fastest rate in milliseconds
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
            //points.add(LatLng(location.latitude, location.longitude))
            val mapPoint = MapPoint(
                LatLng(location.latitude, location.longitude),
                currentLineColor,
                currentLineWidth.toFloat()
            )
            pointsList.add(mapPoint)
            //drawPoint(mapPoint)
            redrawLine()
        }
    }

    private fun drawPoint(mapPoint: MapPoint) {
        options
            .color(mapPoint.color)
            .width(mapPoint.width)
            .geodesic(true)
            .add(mapPoint.coordinate)
        map.addPolyline(options)
    }

    private fun redrawLine() {
        map.clear()
        //options.addAll(points)
//        for (mapPoint in pointsList) {
//            val options = PolylineOptions()
//                .color(mapPoint.color)
//                .width(mapPoint.width)
//                .geodesic(true)
//                .add(mapPoint.coordinate)
//            map.addPolyline(options)
//        }
        showPolyline()
    }

    private fun showPolyline() {
        if (pointsList.size < 2) return
        var ix = 0
        var currentPoint: MapPoint = pointsList[ix]
        var currentColor: Int = currentPoint.color
        var currentWidth = currentPoint.width
        val currentSegment: MutableList<LatLng> = ArrayList()
        currentSegment.add(currentPoint.coordinate)
        ix++
        while (ix < pointsList.size) {
            currentPoint = pointsList[ix]
            if (currentPoint.color == currentColor && currentPoint.width == currentWidth) {
                currentSegment.add(currentPoint.coordinate)
            } else {
                currentSegment.add(currentPoint.coordinate)
                map.addPolyline(
                    PolylineOptions()
                        .addAll(currentSegment)
                        .color(currentColor)
                        .width(currentWidth)
                )
                currentColor = currentPoint.color
                currentWidth = currentPoint.width
                currentSegment.clear()
                currentSegment.add(currentPoint.coordinate)
            }
            ix++
        }
        map.addPolyline(
            PolylineOptions()
                .addAll(currentSegment)
                .color(currentColor)
                .width(currentWidth)
        )
    }

    private fun moveToCurrentLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {               //FIXME: recheck location somehow
                    showToast(
                        "Lat is ${location.latitude} " +
                                "+ Lng is ${location.longitude}"
                    )
                    moveAndZoomCamera(LatLng(location.latitude, location.longitude), 15f)
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
        val asynchronousWorkActivity = myActivity as AsynchronousWorkActivity
        asynchronousWorkActivity.startProgressBar()
        map.snapshot { bitmap ->
            val uri = saveToInternalStorage(bitmap)
            asynchronousWorkActivity.stopProgressBar()
            openMapSnapshotDialog(uri)
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

    private fun openShareImageDialog(filePath: String) {
        val file: File = myActivity.getFileStreamPath(filePath)
        if (filePath != "") {
            val values = ContentValues(2)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            val contentUriFile =
                myActivity.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, contentUriFile)
            startActivity(Intent.createChooser(intent, "Share Image"))
        } else {
            showToast("share image failed")
        }
    }

    private fun openMapSnapshotDialog(imageUri: Uri) {
        MapSnapshotDialog(imageUri).show(childFragmentManager, DIALOG_TAG)
    }

    private fun openMapDrawingSettingsDialog() {
        MapDrawingSettingsDialog(
            currentLineColor,
            currentLineWidth,
            this
        ).show(childFragmentManager, DIALOG_TAG)
    }

    private fun makeOptions(): PolylineOptions {
        return PolylineOptions()
            .color(currentLineColor)
            .width(currentLineWidth.toFloat())
            .geodesic(true)
    }

    private fun finish() {
        if (pointsList.size > 1) {
            addMarker(pointsList[0].coordinate, getString(R.string.start))
            addMarker(pointsList[pointsList.size - 1].coordinate, getString(R.string.finish))
        }
    }

    private fun setTestPointsSet() {
        pointsList.add(
            MapPoint(
                LatLng(53.0957724, 91.41591399),
                Color.MAGENTA,
                8f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.0957724, 91.41591399),
                Color.MAGENTA,
                8f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.0957110, 91.41754799),
                Color.RED,
                15f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.0957110, 91.41754799),
                Color.RED,
                15f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.09531541, 91.41750823),
                Color.GREEN,
                5f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.09531541, 91.41750823),
                Color.GREEN,
                5f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.095293651, 91.41601257),
                Color.BLUE,
                12f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.095293651, 91.41601257),
                Color.BLUE,
                12f
            )
        )
        pointsList.add(
            MapPoint(
                LatLng(53.0957724, 91.41591399),
                Color.MAGENTA,
                8f
            )
        )
        redrawLine()
//        for (mapPoint in pointsList) {
//            drawPoint(mapPoint)
//        }
    }

}

