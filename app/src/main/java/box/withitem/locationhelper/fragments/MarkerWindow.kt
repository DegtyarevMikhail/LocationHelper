package box.withitem.locationhelper.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import box.withitem.locationhelper.App
import box.withitem.locationhelper.BuildConfig
import box.withitem.locationhelper.R
import box.withitem.locationhelper.data.MarkerPoint
import box.withitem.locationhelper.utils.*
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow


class MarkerWindow(
    mapView: MapView,
    private val markerPoint: MarkerPoint,
    activityFragment: FragmentActivity
) : InfoWindow(R.layout.marker_info_window, mapView) {

    private val TAG = "happy"
    private val activityLocal = activityFragment
    private val user by lazy { FirebaseAuth.getInstance().currentUser }

    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)

        val helpGoButton = mView.findViewById<ImageButton>(R.id.help_go_button)
        val editButton = mView.findViewById<ImageButton>(R.id.edit_button)
        val deleteButton = mView.findViewById<ImageButton>(R.id.delete_button)

        if (markerPoint.pointState == PointState.Checked) {

            helpGoButton.background.setColorFilter(
                activityLocal.resources.getColor(R.color.accident_go_button),
                PorterDuff.Mode.MULTIPLY
            )
            val textHelpOnRoad = mView.findViewById<TextView>(R.id.help_banner)
            textHelpOnRoad.isVisible = true
            textHelpOnRoad.text = activityLocal.getText(R.string.help_on_road)
            //textHelpOnRoad.setBackgroundColor(activityLocal.getColor(R.color.accident_go_button))
        }

        val addressInfoPoint = getFullAddress(markerPoint.lat, markerPoint.lon, 1, mapView.context)
        val textAddressEvent = mView.findViewById<TextView>(R.id.address_event)
        textAddressEvent.text = addressInfoPoint

        val serviceInfoMarkerPoint = setDateTimeMarketPoint(markerPoint)
        val serviceInfo = mView.findViewById<TextView>(R.id.service_info)
        serviceInfo.text = serviceInfoMarkerPoint

        val accidentDescriptionInfo =
            getAccidentDescription()[markerPoint.accidentDescription.toString()]
        mView.findViewById<TextView>(R.id.accident_desc_info).text = accidentDescriptionInfo

        val severityAccidentInfo = getSeverityAccident()[markerPoint.severityAccident.toString()]
        mView.findViewById<TextView>(R.id.severity_accident_info).text = severityAccidentInfo

        val statusAmbulance = mView.findViewById<TextView>(R.id.call_ambulance_car_info)
        if (markerPoint.callAmbulance) {
            statusAmbulance.text = activityLocal.getString(R.string.yes_call_ambulance)
        } else {
            statusAmbulance.text = activityLocal.getString(R.string.no_call_ambulance)
        }

        mView.findViewById<TextView>(R.id.note_accident_info).text = markerPoint.accidentNotes

        if (user?.isAnonymous == true) {
            helpGoButton.isVisible = false
            editButton.isVisible = false
            deleteButton.isVisible = false
        }

        helpGoButton.setOnClickListener {
//            Log.d(TAG, "Help GO")

            val pointStateChange: PointState = if (markerPoint.pointState != PointState.Checked) {
                buildRoadToAccident()
                PointState.Checked
            } else {
                deleteRouteToAccident()
                PointState.Base
            }
            val markerPointChangeState = MarkerPoint(
                markerPoint.currentUser,
                markerPoint.accidentType,
                markerPoint.lat,
                markerPoint.lon,
                markerPoint.timestamp,
                pointStateChange,
                markerPoint.accidentDescription,
                markerPoint.severityAccident,
                markerPoint.callAmbulance,
                markerPoint.accidentNotes
            )
            saveConditionOfMarketPoint(markerPointChangeState)

            Log.d(TAG, "Статус после GO: ${markerPointChangeState.pointState}")

        }

        editButton.setOnClickListener {
// TODO вопрос про фрагмент - не будет ли утечка памяти при передаче activity?????
//            requireActivity().supportFragmentManager.apply {
            activityLocal.supportFragmentManager.apply {
                beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                        R.id.placeHolder,
                        MarkerInfoFragment.newInstance(Bundle().apply {
                            putParcelable(MarkerInfoFragment.BUNDLE_EXTRA, markerPoint)
                        })
                    )
//                    .addToBackStack(null)
                    .addToBackStack("")
                    .commit()
            }
        }

        deleteButton.setOnClickListener {
//            Log.d(TAG, "Delete marker")
            deleteMarkerFromView()
        }

        mView.setOnClickListener {
//            Log.d(TAG, "CLOSE window")
            close()
        }
    }

    override fun onClose() {
//        Log.d(TAG, "onClose() called")
    }

    private fun deleteMarkerFromView() {

        val pointStateChange = PointState.NotRelevant
        val markerPointChangeState = MarkerPoint(
            markerPoint.currentUser,
            markerPoint.accidentType,
            markerPoint.lat,
            markerPoint.lon,
            markerPoint.timestamp,
            pointStateChange,
            markerPoint.accidentDescription,
            markerPoint.severityAccident,
            markerPoint.callAmbulance,
            markerPoint.accidentNotes
        )
//        Log.d(TAG, "Статус стал : ${markerPointChangeState.pointState}")
        Log.d(TAG, "Удаление точки : ${markerPointChangeState.pointState}")

        saveConditionOfMarketPoint(markerPointChangeState)
    }

    private fun buildRoadToAccident() {
//        mapView.overlays.forEach {
//            if (it is Polyline && it.id == "road") {
//                mapView.overlays.remove(it)
//                mapView.invalidate()
//            }
//        }
        deleteRouteToAccident()

        val endRoad = markerPoint

        val startPoint = GeoPoint(startRoad.latitude, startRoad.longitude)
        val endPoint = GeoPoint(endRoad.lat, endRoad.lon)

        val roadManager: RoadManager = OSRMRoadManager(mMapView.context, BuildConfig.BUILD_TYPE)
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(GeoPoint(startPoint))
        waypoints.add(GeoPoint(endPoint))
        val road = roadManager.getRoad(waypoints)
        if (road.mStatus != Road.STATUS_OK)
            Toast.makeText(
                mView.context,
                "Ошибка загрузки маршрута - status:" + road.mStatus,
                Toast.LENGTH_SHORT
            ).show();
        val roadOverlay = RoadManager.buildRoadOverlay(road)

//        for (i in 0 until mapView.overlays.size) {
//            val overlay: Overlay = mapView.overlays[i]
//            if (overlay is Polyline && (overlay as Polyline).id == "road") {
//                mapView.overlays.remove(overlay)
//                mapView.invalidate()
//            }
//        }
        roadOverlay.id = "road"
        roadOverlay.outlinePaint.color = Color.BLUE
        roadOverlay.outlinePaint.strokeWidth = 13f
        roadOverlay.outlinePaint.alpha = 130
        mapView.overlays.add(roadOverlay)
/*        //3. Шаги по карте
        val roadMarkers = FolderOverlay()
        //mapView.overlays.remove(roadMarkers)
        mapView.overlays.add(roadMarkers)
        val nodeIcon = ResourcesCompat.getDrawable(mView.resources, R.drawable.flag_green_32, null)
        for (i in 0 until road.mNodes.size) {
            val node: RoadNode = road.mNodes.get(i)
            val nodeMarker = Marker(mapView)
            nodeMarker.position = node.mLocation
            nodeMarker.icon = nodeIcon

            //4. наполняем подсказками маршнут
            nodeMarker.title = "Шаг $i"
            //nodeMarker.id = "road"
            nodeMarker.snippet = node.mInstructions
            nodeMarker.subDescription =
                Road.getLengthDurationText((mMapView.context), node.mLength, node.mDuration)
            val iconContinue =
                ResourcesCompat.getDrawable(mView.resources, R.drawable.motorbike_32, null)
            nodeMarker.image = iconContinue
            // добавили подсказки
            roadMarkers.add(nodeMarker)

        }*/
        closeAllInfoWindowsOn(mapView)
        mapView.invalidate()
    }

    private fun deleteRouteToAccident() {

        mapView.overlays.forEach {
            if (it is Polyline && it.id == "road") {
                mapView.overlays.remove(it)
                mapView.invalidate()
            }
        }
        closeAllInfoWindowsOn(mapView)
        mapView.invalidate()
    }

    private fun saveConditionOfMarketPoint(markerPoint: MarkerPoint) {

        App().dbGeoPoints.child(markerPoint.timestamp.toString())
            .setValue(markerPoint)
            .addOnSuccessListener {

                Log.d(TAG, "Удачно записали в infoWindow")

            }.addOnFailureListener {

                Log.d(TAG, "Не удалось записать в infoWindow")
            }
        closeAllInfoWindowsOn(mapView)
        mapView.invalidate()
    }
}


