package box.withitem.locationhelper.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK

import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import box.withitem.locationhelper.R
import box.withitem.locationhelper.app
import box.withitem.locationhelper.data.Constants.DEFAULT_LOCATIONS_LAT
import box.withitem.locationhelper.data.Constants.DEFAULT_LOCATIONS_LON
import box.withitem.locationhelper.data.Constants.DEFAULT_ZOOM
import box.withitem.locationhelper.data.Constants.REQUEST_CHECK_SETTINGS
import box.withitem.locationhelper.data.MapViewModel
import box.withitem.locationhelper.data.MarkerPoint

import box.withitem.locationhelper.databinding.MapScreenFragmentBinding
import box.withitem.locationhelper.model.location.MyEventLocationSettingsChange
import box.withitem.locationhelper.utils.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsDisplay
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import timber.log.Timber
import java.sql.Timestamp
import java.util.*

lateinit var startRoad: Location


@SuppressLint("RestrictedApi")
class MapScreenFragment : Fragment(R.layout.map_screen_fragment) {
    lateinit var currentLocation: Location

    private lateinit var fusedLocationClient: FusedLocationProviderClient //смотрим здесь

    //    private val geocoder: Geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }
    //   private val map: MapView by lazy { binding.mapView }
    private lateinit var mapfun: MapView


    //    private val mapControllerNew: IMapController by lazy { map.controller }
    private lateinit var mapControllerNew: IMapController
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private var lastLocation: Location? = null
    private var locationCallback: LocationCallback
    private var requestingLocationUpdates = false
    private var startPoint: GeoPoint = GeoPoint(DEFAULT_LOCATIONS_LAT, DEFAULT_LOCATIONS_LON)
    private var marker: Marker? = null
    private var path1: Polyline? = null
    private val locationRequest: LocationRequest by lazy { createLocationRequest() }
    private val preferences: SharedPreferences by lazy { PreferenceManager(requireContext()).sharedPreferences!! }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }
    private val model: MapViewModel by activityViewModels()

    private val TAG = "happy"

    private var _binding: MapScreenFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {

        fun newInstance(): Fragment {
            val args = Bundle()
            val fragment = MapScreenFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    has comments
    init {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let {
                    for (location in locationResult.locations) {
                        updateLocation(location)
                    }
                }
            }
        }

        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            Timber.d("Разрешения предоставлены $allAreGranted")
            Toast(requireActivity()).showCustomToast(
                "Разрешения предоставлены", 0, requireActivity()
            )

            if (allAreGranted) {
                initCheckLocationSettings()
                initMap() //если с настройками всё ок
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree()) //Init report type
        }
        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val nodeIcon = ResourcesCompat.getDrawable(resources, R.drawable.flag_green_32, null)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapfun = binding.mapView
//Выбор TileSource. Меняется во фрагменте настроек. Значения находятся в arrays.xml, задаются в settings_fragment.xml
        when (preferences.getString("MapType", "MAPNIK")) {
            "MAPNIK" -> {mapfun.setTileSource(TileSourceFactory.MAPNIK)
                // для ночного режима, пока не реализованно
                //map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS)
                //map.getOverlayManager().getTilesOverlay().setColorFilter(null)
            }
            "OpenTopo" -> mapfun.setTileSource(TileSourceFactory.OpenTopo)
            "WIKIMEDIA" -> mapfun.setTileSource(TileSourceFactory.WIKIMEDIA)
        }

        model.liveMarker.observe(viewLifecycleOwner){
            if(it == null) return@observe
            mapfun.controller.animateTo(GeoPoint(it.lat, it.lon))
        }

        model.endPointMarker.observe(viewLifecycleOwner){
            if (it == null) return@observe
            buildRoadToAccident(it)
        }

        binding.efAccident.isVisible = false
        binding.efAmbulance.isVisible = false
        binding.efBreakdown.isVisible = false

        mapfun.isTilesScaledToDpi =
            preferences.getBoolean("tileScaleToDPI", true) // изменение масштаба карты
        mapfun.setMultiTouchControls(true) // мультитач управление
        mapControllerNew = mapfun.controller

        val appPerms = arrayOf( // массив разрешений
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        activityResultLauncher.launch(appPerms)

        binding.efEventMarkerAdd.setOnClickListener {
            binding.apply {
                efAmbulance.showFabMarkerFast()
                efBreakdown.showFabMarkerFast()
                efAccident.showFabMarkerFast()
                efEventMarkerAdd.hideFabMarkerFast()
            }
        }

        binding.efCurrentPosition.setOnClickListener {
            mapControllerNew.setCenter(startPoint)
            getPositionMarker().position = startPoint
            mapfun.invalidate()
        }

//        model.getData().observe(viewLifecycleOwner, processDatabaseData())
//        model.getFreshData().observe(viewLifecycleOwner, processDatabaseData())

        model.getFreshRelevantData().observe(viewLifecycleOwner, processDatabaseData())

        // вызов быстрого маркера с выбором типа ДТП
        binding.apply {
            efBreakdown.setOnClick {
                addEvent(AccidentType.Breakdown)
            }
            efAccident.setOnClick {
                addEvent(AccidentType.Accident)
            }
            efAmbulance.setOnClick {
                addEvent(AccidentType.Ambulance)
            }
        }
        if (user?.isAnonymous == true) {
            binding.efEventMarkerAdd.isVisible = false
        } else {
            mapfun.isClickable

            mapfun.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {

                        binding.apply {
                            efAmbulance.hideFabMarkerFast()
                            efBreakdown.hideFabMarkerFast()
                            efAccident.hideFabMarkerFast()
                            efEventMarkerAdd.hideFabMarkerFast()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        binding.efEventMarkerAdd.showFabMarkerFast()
                    }
                }
                false
            })

            binding.efSOS?.setOnClickListener {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.SEND_SMS
                )
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    val phone = preferences.getString("EditTextPhone", "")
                    val message =
                        "Я в беде: https://yandex.ru/maps/?ll=${startPoint.latitude},${startPoint.longitude},z=13"
                    if (phone != null) {
                        mySOSMessage(phone, message, requireActivity())
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Укажите в настройках номер телефона близкого человека",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 101
                    )
                }
            }

        }
    }
    private fun buildRoadToAccident(endRoad: MarkerPoint) {
        deleteRouteToAccident()

        val startPoint = GeoPoint(startRoad.latitude, startRoad.longitude)
        val endPoint = GeoPoint(endRoad.lat, endRoad.lon)

        val roadManager: RoadManager = OSRMRoadManager(requireContext(), box.withitem.locationhelper.BuildConfig.BUILD_TYPE)
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(startPoint)
        waypoints.add(endPoint)
        val road = roadManager.getRoad(waypoints)
        if (road.mStatus != Road.STATUS_OK)
            Toast.makeText(
                requireContext(),
                "Ошибка загрузки маршрута - status:" + road.mStatus,
                Toast.LENGTH_SHORT
            ).show();
        val roadOverlay = RoadManager.buildRoadOverlay(road)

        roadOverlay.id = "road"
        roadOverlay.outlinePaint.color = Color.BLUE
        roadOverlay.outlinePaint.strokeWidth = 13f
        roadOverlay.outlinePaint.alpha = 130
        binding.mapView.overlays.add(roadOverlay)

        //InfoWindow.closeAllInfoWindowsOn(binding.mapView)
        // binding.mapView.invalidate()
    }

    private fun deleteRouteToAccident() {
        binding.mapView.overlays.forEach {
            if (it is Polyline && it.id == "road") {
                binding.mapView.overlays.remove(it)
                binding.mapView.invalidate()
            }
        }
        InfoWindow.closeAllInfoWindowsOn(binding.mapView)
        binding.mapView.invalidate()
    }

    // отслеживаем изменения данных в БД
    private fun processDatabaseData(): Observer<List<MarkerPoint>> {
        val dataObserver = Observer<List<MarkerPoint>> { t ->

            mapfun.overlays.forEach {
                if ((it is Marker) && (it.id != "MyLocation")) {
                    mapfun.overlays.remove(it)
                    mapfun.invalidate()
                }
            }

            t?.forEach { markerPoint ->
                val markerDB = Marker(mapfun, requireContext())
                markerDB.apply {
                    id = markerPoint.timestamp.toString()
                    position.latitude = markerPoint.lat
                    position.longitude = markerPoint.lon
                    icon = when (markerPoint.accidentType) {
                        null -> ResourcesCompat.getDrawable(
                            resources, R.drawable.ico_moto_crash_36, null
                        )
                        AccidentType.Accident -> when (markerPoint.pointState) {
                            PointState.Checked -> ResourcesCompat.getDrawable(
                                resources, R.drawable.ico_moto_crash_36_help, null
                            )
                            PointState.Base, PointState.NotRelevant, null ->
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.ico_moto_crash_36, null
                                )
                        }
                        AccidentType.Ambulance -> when (markerPoint.pointState) {
                            PointState.Checked -> ResourcesCompat.getDrawable(
                                resources, R.drawable.ico_moto_help_36_help, null
                            )
                            PointState.Base, PointState.NotRelevant, null ->
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.ico_moto_help_36, null
                                )
                        }
                        AccidentType.Breakdown -> when (markerPoint.pointState) {
                            PointState.Checked ->
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.ico_moto_breakdown_36_help, null
                                )
                            PointState.Base, PointState.NotRelevant, null ->
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.ico_moto_breakdown_36, null
                                )
                        }
                    }

                    val infoWindow = activity?.let { MarkerWindow(mapfun, markerPoint, it) }

                    this.infoWindow = infoWindow

                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapfun.overlays.add(markerDB)
                mapfun.invalidate()
            }
            mapfun.invalidate()
        }
        return dataObserver
    }

    //    Функция для добавления и отправки маркера.
    private fun addEvent(accidentType: AccidentType) {
        user.let {
            if (!user?.email.isNullOrEmpty() or (user?.isAnonymous == false)) {

                val marker = Marker(mapfun)
                marker.icon = when (accidentType) {
                    AccidentType.Breakdown -> ResourcesCompat.getDrawable(
                        resources, R.drawable.ico_moto_breakdown_36, null
                    )
                    AccidentType.Accident -> ResourcesCompat.getDrawable(
                        resources, R.drawable.ico_moto_crash_36, null
                    )
                    AccidentType.Ambulance -> ResourcesCompat.getDrawable(
                        resources, R.drawable.ico_moto_help_36, null
                    )
                }

                marker.position.latitude = startPoint.latitude
                marker.position.longitude = startPoint.longitude

                marker.title =
                    "lan: ${marker.position.latitude}, lon: ${marker.position.longitude}\n time: ${dateTimeEvent()}"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                mapfun.overlays.add(marker)
                mapfun.invalidate()

//            Добавление маркера в БД. timestamp - id точки в БД.
                val timestamp = Timestamp(Date().time).time

                app.dbGeoPoints.child(timestamp.toString()).setValue(
                    MarkerPoint(
                        user?.email!!,
                        accidentType,
                        marker.position.latitude,
                        marker.position.longitude,
                        timestamp,
                        pointState = PointState.Base
                    )
                )
            }
        }
        binding.apply {
            efAmbulance.hideFabMarkerFast()
            efBreakdown.hideFabMarkerFast()
            efAccident.hideFabMarkerFast()
            efEventMarkerAdd.showFabMarkerFast()
        }
    }

    fun setMyLocation() {
        Toast.makeText(requireContext(), "Location", Toast.LENGTH_SHORT).show()
        mapControllerNew.setCenter(startPoint)
        getPositionMarker().position = startPoint
        mapfun.invalidate()
    }

    //has comments
    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            smallestDisplacement = 10f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }

    }

    //has comments
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsg(status: MyEventLocationSettingsChange) {
        if (status.on) {
            initMap()
        } else {
            Timber.i("Stop something")
        }
    }

    //
    private fun initLocation() { //call in create
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())//getActivity() = this
        readLastKnownLocation()
    }

    //has comments
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() { //используем в onPause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //has comments
    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { updateLocation(it) }
        }
    }

    // взял с документации библиотеки
    private fun initCheckLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Toast(requireActivity()).showCustomToast(
                resources.getString(R.string.init_check_location_settings_message_ok),
                0,
                requireActivity()
            )
            Timber.d(resources.getString(R.string.init_check_location_settings_message_ok))
            MyEventLocationSettingsChange.globalState = true //default
            initMap()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {

                Timber.d(resources.getString(R.string.add_on_failure_listener))
                try {

                    exception.startResolutionForResult(
                        requireActivity(), REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                    Timber.d("Settings Location sendEx??")
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Settings onActivityResult for $requestCode result $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                initMap()
            }
        }
    }

    //     обновляем текущую геопозицию
    fun updateLocation(newLocation: Location) {
        lastLocation = newLocation
        binding.tvDebugInfo.text = buildString {
            append("lat:${newLocation.latitude}\n")
            append("lon:${newLocation.longitude}\n")
            append("speed (m/sec):${newLocation.speed}\n")
        }
        binding.tvSpeedText.text = buildString {
            append(((newLocation.speed) * 60 * 60 / 1000).toInt())
            append("km")
        }
        startRoad = newLocation
        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        mapControllerNew.setCenter(startPoint)
        getPositionMarker().position = startPoint
        mapfun.invalidate()
    }

    //has comments
    private fun initMap() {
        initLocation()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }

        mapControllerNew.setZoom(DEFAULT_ZOOM)
        mapfun.zoomController.display.setPositions(
            false, CustomZoomButtonsDisplay.HorizontalPosition.RIGHT, CustomZoomButtonsDisplay.VerticalPosition.CENTER
        )
        mapfun.overlays.add(CopyrightOverlay(requireContext())) //копирайт
        mapControllerNew.setCenter(startPoint)
        mapfun.invalidate()
    }

    //     отрисовка маршрута проезда
    private fun getPath(): Polyline { //Singleton
        if (path1 == null) {
            path1 = Polyline()
            with(path1!!) {
                outlinePaint.color = Color.RED
                outlinePaint.strokeWidth = 10f
                addPoint(startPoint.clone())
            }
            mapfun.overlayManager.add(path1)
        }
        return path1!!
    }

    //     маркер текущей геопозиции
    private fun getPositionMarker(): Marker { //Singleton
// ===
/*

        if (marker == null) {
            marker = Marker(map)

            with(marker!!) {
                id = "MyLocation"
                title =
                    "lan: ${startPoint.latitude}, lon: ${startPoint.longitude}\n time: ${dateTimeEvent()}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = ResourcesCompat.getDrawable(resources, (R.drawable.dot), null)
            }
            Log.d(TAG, "getPositionMarker() called: $marker")
//            map.overlays.add(marker)
        }

*/


        // ===
        if (marker == null) {
            marker = Marker(mapfun)
//            Log.d(TAG, "getPositionMarker() called: $marker")
        }
        marker!!.apply {
            id = "MyLocation"
            title =
                "lan: ${startPoint.latitude}, lon: ${startPoint.longitude}\n time: ${dateTimeEvent()}"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = ResourcesCompat.getDrawable(resources, (R.drawable.dot), null)
        }

        mapfun.overlays.add(marker)

//        Log.d(TAG, "getPositionMarker() marker ЕСТЬ ===========> ${marker!!.id}")

        return marker!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = MapScreenFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initMap()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates() // отстанавливаем обновление геопозиции
        }
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

