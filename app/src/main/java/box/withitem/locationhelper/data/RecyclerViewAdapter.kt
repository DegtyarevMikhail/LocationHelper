package box.withitem.locationhelper.data

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import box.withitem.locationhelper.R
import box.withitem.locationhelper.databinding.ListItemBinding
import box.withitem.locationhelper.utils.*
import java.util.*
import com.google.firebase.auth.FirebaseAuth


class RecyclerViewAdapter(private val listener: Listener) :
    ListAdapter<MarkerPoint, RecyclerViewAdapter.RecyclerViewHolder>(ExampleItemDiffCallback) {


    class RecyclerViewHolder(
        private val itemBinding: ListItemBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        private val user by lazy { FirebaseAuth.getInstance().currentUser }

        /* Bind league name and image. */
        @SuppressLint("SimpleDateFormat")
        fun bind(markerPoint: MarkerPoint, listener: Listener) = with(itemBinding) {

            if (user?.isAnonymous == false) {
                itemView.setOnClickListener {
                    listener.onClick(markerPoint)
                }
            }

            imageViewHelp.setOnClick {
                listener.onClickCreateRoad(markerPoint)
            }

            imageViewGoToMarker.setOnClickListener {
                listener.onClickImage(markerPoint)
            }

            twEventTime.text = setDateTimeMarketPoint(markerPoint)

            val addressText =
                getFullAddress(markerPoint.lat, markerPoint.lon, 1, imageViewAmbulance.context)!!
            twAddress.text = addressText

            imageViewShare.setOnClickListener {
                val string = "${markerPoint.lat},${markerPoint.lon}"
                shareUrl(it.context, string)
            }

            twWhatHappened.text =
                getAccidentDescription()[markerPoint.accidentDescription.toString()]


            twAftermathInfo.text = getSeverityAccident()[markerPoint.severityAccident.toString()]

            if (!markerPoint.accidentNotes.isNullOrEmpty()) {
                twNote.isVisible = true
                twNote.text = markerPoint.accidentNotes
            }

            if (markerPoint.callAmbulance) {
                imageViewAmbulance.setImageResource(
                    R.drawable.ic_baseline_add_alert_24_red
                )
            } else {
                imageViewAmbulance.setImageResource(
                    R.drawable.ic_baseline_add_alert_24_grey
                )
            }

            if (markerPoint.pointState == PointState.Checked) {
                imageViewHelp.setBackgroundResource(R.color.green)
            } else {
                imageViewHelp.background = null
            }

            if (markerPoint.accidentType == AccidentType.Accident) {
                conLayout.setBackgroundResource(R.color.card_accident)
            }
            if (markerPoint.accidentType == AccidentType.Breakdown) {
                conLayout.setBackgroundResource(R.color.card_breakdown)
            }
            if (markerPoint.accidentType == AccidentType.Ambulance) {
                conLayout.setBackgroundResource(R.color.card_ambulance)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ListItemBinding.inflate(layoutInflater, parent, false)
        return RecyclerViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        val logs = getItem(position)
        holder.bind(logs, listener)

    }

    object ExampleItemDiffCallback : DiffUtil.ItemCallback<MarkerPoint>() {
        override fun areItemsTheSame(oldItem: MarkerPoint, newItem: MarkerPoint): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MarkerPoint, newItem: MarkerPoint): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }
    }


    interface Listener {
        fun onClick(marker: MarkerPoint)
        fun onClickImage(marker: MarkerPoint)
        fun onClickCreateRoad(marker: MarkerPoint)
    }

}
