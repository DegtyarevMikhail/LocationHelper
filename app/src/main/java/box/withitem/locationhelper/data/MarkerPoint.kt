package box.withitem.locationhelper.data

import android.os.Parcelable
import box.withitem.locationhelper.utils.AccidentDescription
import box.withitem.locationhelper.utils.AccidentType
import box.withitem.locationhelper.utils.PointState
import box.withitem.locationhelper.utils.SeverityAccident
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MarkerPoint(
    val currentUser: String = "",
    val accidentType: AccidentType? = AccidentType.Accident,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val timestamp: Long? = 0L,
    val pointState: PointState? = PointState.Base,
    val accidentDescription: AccidentDescription? = AccidentDescription.None,
    val severityAccident: SeverityAccident? = SeverityAccident.None,
    val callAmbulance: Boolean = false,
    val accidentNotes: String? = null
): Parcelable


