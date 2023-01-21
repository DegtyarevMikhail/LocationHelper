package box.withitem.locationhelper

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import box.withitem.locationhelper.data.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class App : Application() {

    private val firebaseDB: FirebaseDatabase by lazy { Firebase.database("https://locationhelper-c20ba-default-rtdb.asia-southeast1.firebasedatabase.app/") }
    val dbGeoPoints: DatabaseReference by lazy { firebaseDB.getReference(Constants.REFERENCE_GROUP) }
}

val Context.app: App get() = applicationContext as App
val Fragment.app: App get() = requireContext().applicationContext as App