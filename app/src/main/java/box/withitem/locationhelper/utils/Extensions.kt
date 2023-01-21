package box.withitem.locationhelper.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import box.withitem.locationhelper.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.util.*


fun Fragment.showFragment(f: Fragment) {
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        .addToBackStack(f.id.toString())
        //.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.placeHolder, f)
        .commit()
}


fun AppCompatActivity.showFragment(f: Fragment) {
    // if (supportFragmentManager.fragments.isNotEmpty()){
    //     if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
    // }
    supportFragmentManager
        .beginTransaction()
        .addToBackStack(f.id.toString())
        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        .replace(R.id.placeHolder, f)
        .commit()
}

// нажатие для MotionLayout - анимация, а потом клик
@SuppressLint("ClickableViewAccessibility")
fun View.setOnClick(clickEvent: () -> Unit) {
    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            clickEvent.invoke()
        }
        false
    }
}

// анимация FAB кнопок быстрого маркера - развернуть
fun ExtendedFloatingActionButton.showFabMarkerFast() {
    this.apply {
        animate().cancel()
        scaleX = 0f
        scaleY = 0f
        alpha = 0f
        isVisible = true
        animate().setDuration(400).scaleX(1f).scaleY(1f).alpha(0.8f).start()
    }
}

// анимация FAB кнопок быстрого маркера - свернуть
fun ExtendedFloatingActionButton.hideFabMarkerFast() {
    this.apply {
        animate().cancel()
        scaleX = 1f
        scaleY = 1f
        alpha = 0.8f
        animate().setDuration(300).scaleX(0f).scaleY(0f).alpha(0f).start()
        isVisible = false
    }
}

// получение списка адресов
// в замен val addresses = geocoder.getFromLocation(markerPoint.lat, markerPoint.lon, 1)
fun getAddressResult(lat: Double, lon: Double, int: Int, context: Context): List<Address>? {
    var geocoder = Geocoder(context, Locale.getDefault())
    val addresses =
        geocoder.getFromLocation(lat, lon, 1)
    return addresses
}

// получение списка адреса
// в замен val addressPoint = addresses?.get(0)?.getAddressLine(0)
fun fullAddress(addresses: List<Address>?): String? {
    val addressPoint = addresses?.get(0)?.getAddressLine(0)
    return addressPoint
}

// полный адрес, если он есть, получаем в виде строки? int = 1
fun getFullAddress(lat: Double, lon: Double, int: Int, context: Context): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(lat, lon, 1)
    val addressPoint = addresses?.get(0)?.getAddressLine(0)
    return addressPoint
}

fun mySOSMessage(phone: String, message: String, context: Context) {
    val message = message
    val textMessageSent = context.resources.getText(R.string.message_sent)
    val textMessageOnError = context.resources.getText(R.string.message_error_sos)
    try {
        // on below line we are initializing sms manager.
        val smsManager: SmsManager = SmsManager.getDefault()
        // on below line we are sending text message.
        smsManager.sendTextMessage(phone, null, message, null, null)
        // on below line we are displaying a toast message for message send,
//        Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG).show()
        Toast.makeText(context, "$textMessageSent", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        // on catch block we are displaying toast message for error.
        Toast.makeText(
            context,
            "$textMessageOnError" + e.message.toString(),
            Toast.LENGTH_LONG
        ).show()
    }
}


/*
fun shareUrl(context: Context, addressText: String) {
*/
/*  отправляем в гугл карты
    val gmmIntentUri = Uri.parse("geo:${addressText}")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)*//*



    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "https://maps.google.com/?q=${addressText}")
        putExtra(Intent.EXTRA_SUBJECT, "${R.string.text_for_share}")
        type = "text/plain"
    }
    val shareIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        Intent.createChooser(sendIntent, "${R.string.text_for_share}", null)
    } else {
        Intent.createChooser(sendIntent, "${R.string.text_for_share}", null)
    }
    context.startActivity(shareIntent)
}
*/

fun isDarkTheme(activity: Activity): Boolean {
    return activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
