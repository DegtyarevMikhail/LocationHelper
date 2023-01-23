package box.withitem.locationhelper.data

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import box.withitem.locationhelper.App
import box.withitem.locationhelper.utils.PointState
import com.google.firebase.database.*
import java.util.*

class MapViewModel(
    val endPointMarker: MutableLiveData<MarkerPoint?> = MutableLiveData(),
    val liveMarker: MutableLiveData<MarkerPoint?> = MutableLiveData(),
    val dbLiveData: MutableLiveData<List<MarkerPoint>> = MutableLiveData(),
    private val dbReference: DatabaseReference = App().dbGeoPoints
) :
    ViewModel() {


    fun getFreshRelevantData(): MutableLiveData<List<MarkerPoint>> {

        val query: Query = dbReference
            .orderByChild("timestamp")
            .startAt(Date().time.toDouble() - 3600000.0)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sss = snapshot.children.mapNotNull { it.getValue(MarkerPoint::class.java) }.toMutableList()
                sss.removeIf { it.pointState == PointState.NotRelevant }
                dbLiveData.value = sss
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(App().applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        return dbLiveData
    }

    fun getFreshData(): MutableLiveData<List<MarkerPoint>> {

        val query: Query = dbReference
            .orderByChild("timestamp")
            .startAt(Date().time.toDouble() - 3600000.0)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sss = snapshot.children.mapNotNull { it.getValue(MarkerPoint::class.java) }
                dbLiveData.value = sss
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(App().applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        return dbLiveData
    }

    fun getData(): MutableLiveData<List<MarkerPoint>> {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sss = snapshot.children.mapNotNull { it.getValue(MarkerPoint::class.java) }
                dbLiveData.value = sss
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(App().applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        return dbLiveData
    }
}