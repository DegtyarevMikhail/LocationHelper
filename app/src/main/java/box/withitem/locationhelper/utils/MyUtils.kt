package box.withitem.locationhelper.utils

import android.app.Activity

import android.graphics.Color

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

import box.withitem.locationhelper.R
import box.withitem.locationhelper.data.MarkerPoint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


    fun Toast.showCustomToast(message: String, color: Int, activity: Activity) {
        val layout = activity.layoutInflater.inflate(
            R.layout.custom_toast,
            activity.findViewById(R.id.toast_container)
        )
        val toast_color = layout.findViewById<FrameLayout>(R.id.button_accent_border)
        when (color) {
            0 -> toast_color.setBackgroundColor(Color.GREEN);
            1 -> toast_color.setBackgroundColor(Color.GRAY);
            2 -> toast_color.setBackgroundColor(Color.RED);
        }

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        this.apply {
            setGravity(Gravity.BOTTOM, 0, 40)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    fun dateTimeFromTimeStamp(timestamp: Long): String {
//    return SimpleDateFormat("HH:mm:ss dd.MM.yy", Locale.getDefault()).format(timestamp) // старая форма
        return SimpleDateFormat("dd.MM.yy / HH:mm", Locale.getDefault()).format(timestamp)
    }

    fun dateTimeEvent(): String { // функция даты/времени для якоря маркера
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm:ss a"))
    }

    private fun dayFromTimeStamp(timestamp: Long): String {
        return SimpleDateFormat("dd-MM-yy", Locale.UK).format(timestamp)
    }

    private fun timeFromTimeStamp(timestamp: Long): String {
        return SimpleDateFormat("HH-mm-ss", Locale.UK).format(timestamp)
    }

    // установка широты, долготы, даты, времени в один String
    fun setServiceInfoMarkerPoint(markerPoint: MarkerPoint): String {
        val serviceInfoMarkerPoint = if (markerPoint.timestamp != null) {
            "lan: ${markerPoint.lat},\nlon: ${markerPoint.lon}\ntime: ${
                dateTimeFromTimeStamp(markerPoint.timestamp)
            }"
        } else {
            "lan: ${markerPoint.lat}, \nlon: ${markerPoint.lat}\ntime: ${dateTimeEvent()}"
        }
        return serviceInfoMarkerPoint
    }

    // установка даты, времени из маркера в один String
    fun setDateTimeMarketPoint(markerPoint: MarkerPoint): String {
        val serviceInfoMarkerPoint = if (markerPoint.timestamp != null) {
            "Дата/Время: ${dateTimeFromTimeStamp(markerPoint.timestamp)}"
        } else {
            "Дата/Время: ${dateTimeEvent()}"
        }
        return serviceInfoMarkerPoint
    }

// Возвращает сколько раз строка символов из паттерна встречалась в строке
    fun countMatches(string: String, pattern: String): Int {
        var index = 0
        var count = 0

        while (true) {
            index = string.indexOf(pattern, index)
            index += if (index != -1) {
                count++
                pattern.length
            } else {
                return count
            }
        }
    }
